package minimarket.etl

import minimarket.application.AppConfig
import minimarket.data.model.Articulo
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

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
    println("   -> TiempoKey generado: $tiempoKey")
    println()

    println("Paso 3: Sincronizando Dim_Articulo...")
    val articuloKeys = sincronizarDimArticulo(articulos)
    println("   -> Articulos sincronizados: ${articuloKeys.size}")
    println()

    println("Paso 4: Cargando datos en Fact_Inventario (MERGE)...")
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

private fun extraerArticulos(): List<Articulo> {
    val articulos = mutableListOf<Articulo>()
    var conn: Connection? = null
    var stmt: PreparedStatement? = null
    var rs: ResultSet? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL)
        stmt = conn.prepareStatement("SELECT ID, Descripcion, Precio, Stock FROM Articulos")
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

private fun insertarFechaActual(): Int {
    val hoy = java.time.LocalDate.now()
    val fechaStr = hoy.toString()
    var conn: Connection? = null
    var stmt: PreparedStatement? = null
    var rs: ResultSet? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL_DW)
        stmt = conn.prepareStatement(
            "SELECT TiempoKey FROM Dim_Tiempo WHERE Fecha = ?"
        )
        stmt.setString(1, fechaStr)
        rs = stmt.executeQuery()
        if (rs.next()) {
            println("   -> Fecha ya existe en Dim_Tiempo, reusing key...")
            return rs.getInt("TiempoKey")
        }
        rs.close()
        stmt.close()

        stmt = conn.prepareStatement(
            "INSERT INTO Dim_Tiempo (Fecha, Anio, Mes, Dia) VALUES (?, ?, ?, ?); SELECT SCOPE_IDENTITY() AS TiempoKey;"
        )
        stmt.setString(1, fechaStr)
        stmt.setInt(2, hoy.year)
        stmt.setInt(3, hoy.monthValue)
        stmt.setInt(4, hoy.dayOfMonth)
        rs = stmt.executeQuery()
        rs.next()
        return rs.getInt("TiempoKey")
    } catch (e: Exception) {
        println("   ERROR al insertar fecha: ${e.message}")
        return -1
    } finally {
        rs?.close()
        stmt?.close()
        conn?.close()
    }
}

private fun sincronizarDimArticulo(articulos: List<Articulo>): List<Int> {
    val keys = mutableListOf<Int>()
    var conn: Connection? = null
    var stmt: PreparedStatement? = null
    var rs: ResultSet? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL_DW)
        for (articulo in articulos) {
            stmt = conn.prepareStatement("SELECT ArticuloKey FROM Dim_Articulo WHERE ArticuloID = ?")
            stmt.setInt(1, articulo.id)
            rs = stmt.executeQuery()
            if (rs.next()) {
                val keyExistente = rs.getInt("ArticuloKey")
                keys.add(keyExistente)
                rs.close()
                stmt.close()

                stmt = conn.prepareStatement(
                    "UPDATE Dim_Articulo SET Descripcion = ?, Precio = ? WHERE ArticuloKey = ?"
                )
                stmt.setString(1, articulo.descripcion)
                stmt.setDouble(2, articulo.precio)
                stmt.setInt(3, keyExistente)
                stmt.executeUpdate()
                print("   ↻ UPDATE Articulo ID=${articulo.id}")
            } else {
                rs.close()
                stmt.close()
                stmt = conn.prepareStatement(
                    "INSERT INTO Dim_Articulo (ArticuloID, Descripcion, Precio) VALUES (?, ?, ?); SELECT SCOPE_IDENTITY() AS ArticuloKey;"
                )
                stmt.setInt(1, articulo.id)
                stmt.setString(2, articulo.descripcion)
                stmt.setDouble(3, articulo.precio)
                rs = stmt.executeQuery()
                rs.next()
                val newKey = rs.getInt("ArticuloKey")
                keys.add(newKey)
                print("   ✚ INSERT Articulo ID=${articulo.id}")
            }
            println(" [${articulo.descripcion.trim()}]")
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
                "SELECT COUNT(*) FROM Fact_Inventario WHERE TiempoKey = ? AND ArticuloKey = ?"
            )
            stmt.setInt(1, tiempoKey)
            stmt.setInt(2, articuloKey)
            rs = stmt.executeQuery()
            rs.next()
            val existe = rs.getInt(1) > 0
            rs.close()
            stmt.close()

            if (existe) {
                stmt = conn.prepareStatement(
                    "UPDATE Fact_Inventario SET Stock = ?, PrecioActual = ? WHERE TiempoKey = ? AND ArticuloKey = ?"
                )
                stmt.setInt(1, articulo.stock)
                stmt.setDouble(2, articulo.precio)
                stmt.setInt(3, tiempoKey)
                stmt.setInt(4, articuloKey)
                stmt.executeUpdate()
                print("   ↻ UPDATE Fact_Inventario ArticuloKey=$articuloKey")
            } else {
                stmt = conn.prepareStatement(
                    "INSERT INTO Fact_Inventario (TiempoKey, ArticuloKey, Stock, PrecioActual) VALUES (?, ?, ?, ?)"
                )
                stmt.setInt(1, tiempoKey)
                stmt.setInt(2, articuloKey)
                stmt.setInt(3, articulo.stock)
                stmt.setDouble(4, articulo.precio)
                stmt.executeUpdate()
                print("   ✚ INSERT Fact_Inventario ArticuloKey=$articuloKey")
                insertados++
            }
            stmt.close()
            println(" [Stock=${articulo.stock}, Precio=${articulo.precio}]")
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