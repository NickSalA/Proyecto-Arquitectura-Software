package minimarket.etl

import minimarket.application.AppConfig
import minimarket.data.model.Articulo
import java.sql.Connection
import java.sql.Date
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Proceso ETL del Entregable 2.
 *
 * Flujo general:
 * 1. Extrae articulos desde MinimarketDB, que es la base operacional usada por el cliente Swing.
 * 2. Crea o recupera la fecha actual en Dim_Tiempo del Datawarehouse.
 * 3. Sincroniza Dim_Articulo usando MERGE, equivalente a un UPSERT en SQL Server.
 * 4. Carga Fact_Inventario usando MERGE para insertar o actualizar el stock del dia.
 *
 * La regla de infraestructura del curso exige manejo imperativo de JDBC: cada funcion abre
 * Connection, PreparedStatement y ResultSet en variables anulables, ejecuta su bloque try y
 * cierra explicitamente los recursos en finally con ?.close(), sin cierre automatico por extension.
 */
fun main() {
    println("╔══════════════════════════════════════════════════╗")
    println("║       MINIMARKET POS - GenerarDatawarehouse      ║")
    println("║                 Proceso ETL v1.0                 ║")
    println("╚══════════════════════════════════════════════════╝")
    println()

    println("Paso 1: Extrayendo articulos desde MinimarketDB...")
    val articulos = extraerArticulos()
    println("   -> Articulos encontrados: ${articulos.size}")
    println()

    if (articulos.isEmpty()) {
        println("   No hay articulos para procesar. Ejecute la aplicacion principal primero.")
        return
    }

    println("Paso 2: Insertando fecha actual en Dim_Tiempo...")
    val tiempoKey = insertarFechaActual()
    if (tiempoKey <= 0) {
        println("   ERROR: No se pudo obtener TiempoKey. El ETL se detiene.")
        return
    }
    println("   -> TiempoKey generado: $tiempoKey")
    println()

    println("Paso 3: Sincronizando Dim_Articulo con MERGE...")
    val articuloKeys = sincronizarDimArticulo(articulos)
    if (articuloKeys.size != articulos.size) {
        println("   ERROR: No todos los articulos fueron sincronizados. El ETL se detiene.")
        return
    }
    println("   -> Articulos sincronizados: ${articuloKeys.size}")
    println()

    println("Paso 4: Cargando datos en Fact_Inventario con MERGE...")
    val insertados = cargarFactInventario(tiempoKey, articuloKeys, articulos)
    val actualizados = articuloKeys.size - insertados
    println("   -> Nuevos registros: $insertados")
    println("   -> Registros actualizados: $actualizados")
    println()

    println("═════════════ RESUMEN DEL ETL ═════════════")
    println("   Fecha procesada: ${java.time.LocalDate.now()}")
    println("   Articulos en origen: ${articulos.size}")
    println("   TiempoKey: $tiempoKey")
    println("   Total filas en Fact_Inventario: ${articuloKeys.size}")
    println("═════════════════════════════════════════════")
}

/**
 * Fase Extract.
 *
 * Lee la tabla Articulos desde MinimarketDB mediante JDBC. Esta consulta es la frontera entre
 * el sistema transaccional Cliente/Servidor y el proceso analitico; por eso solo se extraen las
 * columnas necesarias para dimensiones y hechos: ID, Descripcion, Precio y Stock.
 */
private fun extraerArticulos(): List<Articulo> {
    val articulos = mutableListOf<Articulo>()
    var conn: Connection? = null
    var stmt: PreparedStatement? = null
    var rs: ResultSet? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL)
        stmt = conn.prepareStatement("SELECT ID, Descripcion, Precio, Stock FROM Articulos ORDER BY ID")
        rs = stmt.executeQuery()
        while (rs.next()) {
            articulos.add(
                Articulo(
                    id = rs.getInt("ID"),
                    descripcion = rs.getString("Descripcion"),
                    precio = rs.getDouble("Precio"),
                    stock = rs.getInt("Stock")
                )
            )
        }
    } catch (e: Exception) {
        println("   ERROR al extraer articulos: ${e.message}")
    } finally {
        rs?.close()
        stmt?.close()
        conn?.close()
    }
    return articulos
}

/**
 * Fase Transform para la dimension de tiempo.
 *
 * El Datawarehouse debe tener una sola fila por fecha. Primero se consulta Dim_Tiempo por la
 * fecha actual; si ya existe se reutiliza su clave subrogada TiempoKey. Si no existe, se inserta
 * la fecha descompuesta en Anio, Mes y Dia, y se recupera la clave generada con OUTPUT inserted.
 */
private fun insertarFechaActual(): Int {
    val hoy = java.time.LocalDate.now()
    val fechaSql = Date.valueOf(hoy)
    var conn: Connection? = null
    var stmt: PreparedStatement? = null
    var rs: ResultSet? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL_DW)

        stmt = conn.prepareStatement("SELECT TiempoKey FROM Dim_Tiempo WHERE Fecha = ?")
        stmt.setDate(1, fechaSql)
        rs = stmt.executeQuery()
        if (rs.next()) {
            println("   -> Fecha ya existe en Dim_Tiempo, se reutiliza la clave.")
            return rs.getInt("TiempoKey")
        }

        rs.close()
        stmt.close()

        stmt = conn.prepareStatement(
            """
            INSERT INTO Dim_Tiempo (Fecha, Anio, Mes, Dia)
            OUTPUT inserted.TiempoKey
            VALUES (?, ?, ?, ?)
            """.trimIndent()
        )
        stmt.setDate(1, fechaSql)
        stmt.setInt(2, hoy.year)
        stmt.setInt(3, hoy.monthValue)
        stmt.setInt(4, hoy.dayOfMonth)
        rs = stmt.executeQuery()
        if (rs.next()) {
            return rs.getInt(1)
        }
        return -1
    } catch (e: Exception) {
        println("   ERROR al insertar fecha: ${e.message}")
        return -1
    } finally {
        rs?.close()
        stmt?.close()
        conn?.close()
    }
}

/**
 * Fase Transform/Load de Dim_Articulo.
 *
 * MERGE funciona como UPSERT:
 * - WHEN MATCHED actualiza Descripcion y Precio si el ArticuloID ya existe.
 * - WHEN NOT MATCHED inserta una nueva dimension con el ArticuloID de negocio.
 * - OUTPUT devuelve la accion aplicada y la clave subrogada ArticuloKey, que luego se usa
 *   para relacionar la tabla de hechos Fact_Inventario.
 */
private fun sincronizarDimArticulo(articulos: List<Articulo>): List<Int> {
    val keys = mutableListOf<Int>()
    var conn: Connection? = null
    var stmt: PreparedStatement? = null
    var rs: ResultSet? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL_DW)
        for (articulo in articulos) {
            stmt = conn.prepareStatement(
                """
                MERGE Dim_Articulo AS destino
                USING (VALUES (?, ?, ?)) AS origen (ArticuloID, Descripcion, Precio)
                    ON destino.ArticuloID = origen.ArticuloID
                WHEN MATCHED THEN
                    UPDATE SET Descripcion = origen.Descripcion, Precio = origen.Precio
                WHEN NOT MATCHED THEN
                    INSERT (ArticuloID, Descripcion, Precio)
                    VALUES (origen.ArticuloID, origen.Descripcion, origen.Precio)
                OUTPUT ${'$'}action AS Accion, inserted.ArticuloKey;
                """.trimIndent()
            )
            stmt.setInt(1, articulo.id)
            stmt.setString(2, articulo.descripcion)
            stmt.setDouble(3, articulo.precio)
            rs = stmt.executeQuery()

            if (rs.next()) {
                val accion = rs.getString("Accion")
                val articuloKey = rs.getInt("ArticuloKey")
                keys.add(articuloKey)
                print("   ${formatearAccionMerge(accion)} Dim_Articulo ArticuloID=${articulo.id}")
                println(" [ArticuloKey=$articuloKey, ${articulo.descripcion.trim()}]")
            }

            rs.close()
            rs = null
            stmt.close()
            stmt = null
        }
    } catch (e: Exception) {
        println("   ERROR al sincronizar Dim_Articulo: ${e.message}")
    } finally {
        rs?.close()
        stmt?.close()
        conn?.close()
    }
    return keys
}

/**
 * Fase Load de la tabla de hechos.
 *
 * Fact_Inventario registra las metricas de inventario por fecha y articulo. El MERGE evita
 * duplicar filas para la misma combinacion TiempoKey + ArticuloKey:
 * - Si la fila existe, actualiza Stock y PrecioActual con los valores actuales del origen.
 * - Si la fila no existe, inserta la medicion del dia.
 */
private fun cargarFactInventario(tiempoKey: Int, articuloKeys: List<Int>, articulos: List<Articulo>): Int {
    var insertados = 0
    var conn: Connection? = null
    var stmt: PreparedStatement? = null
    var rs: ResultSet? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL_DW)
        for (i in articuloKeys.indices) {
            val articuloKey = articuloKeys[i]
            val articulo = articulos[i]

            stmt = conn.prepareStatement(
                """
                MERGE Fact_Inventario AS destino
                USING (VALUES (?, ?, ?, ?)) AS origen (TiempoKey, ArticuloKey, Stock, PrecioActual)
                    ON destino.TiempoKey = origen.TiempoKey
                    AND destino.ArticuloKey = origen.ArticuloKey
                WHEN MATCHED THEN
                    UPDATE SET Stock = origen.Stock, PrecioActual = origen.PrecioActual
                WHEN NOT MATCHED THEN
                    INSERT (TiempoKey, ArticuloKey, Stock, PrecioActual)
                    VALUES (origen.TiempoKey, origen.ArticuloKey, origen.Stock, origen.PrecioActual)
                OUTPUT ${'$'}action AS Accion;
                """.trimIndent()
            )
            stmt.setInt(1, tiempoKey)
            stmt.setInt(2, articuloKey)
            stmt.setInt(3, articulo.stock)
            stmt.setDouble(4, articulo.precio)
            rs = stmt.executeQuery()

            if (rs.next()) {
                val accion = rs.getString("Accion")
                if (accion == "INSERT") {
                    insertados++
                }
                print("   ${formatearAccionMerge(accion)} Fact_Inventario ArticuloKey=$articuloKey")
                println(" [Stock=${articulo.stock}, Precio=${articulo.precio}]")
            }

            rs.close()
            rs = null
            stmt.close()
            stmt = null
        }
    } catch (e: Exception) {
        println("   ERROR al cargar Fact_Inventario: ${e.message}")
    } finally {
        rs?.close()
        stmt?.close()
        conn?.close()
    }
    return insertados
}

/**
 * Convierte la accion textual de SQL Server MERGE en una etiqueta legible para consola.
 */
private fun formatearAccionMerge(accion: String): String {
    return when (accion) {
        "INSERT" -> "✚ INSERT"
        "UPDATE" -> "↻ UPDATE"
        else -> accion
    }
}
