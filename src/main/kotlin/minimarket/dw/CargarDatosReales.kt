package minimarket.dw

import minimarket.config.AppConfig
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.time.LocalDate

data class ProductoData(
    val id: Int,
    val descripcion: String,
    val precioNormal: Double,
    val stocks: Map<LocalDate, Int>,
    val precios: Map<LocalDate, Double>
)

fun main() {
    println("╔══════════════════════════════════════════════════╗")
    println("║    MINIMARKET POS - Cargar Datos Reales         ║")
    println("║     Reinicia datos con productos realistas      ║")
    println("╚══════════════════════════════════════════════════╝")
    println()

    val anteayer = LocalDate.of(2026, 6, 15)
    val ayer = LocalDate.of(2026, 6, 16)
    val hoy = LocalDate.now()
    val fechas = listOf(anteayer, ayer, hoy)

    val productos = listOf(
        ProductoData(1, "Arroz Granero de Oro 1kg", 4.50,
            mapOf(anteayer to 50, ayer to 48, hoy to 45),
            mapOf(anteayer to 4.50, ayer to 4.50, hoy to 4.50)),
        ProductoData(2, "Leche Ideal 1L", 3.20,
            mapOf(anteayer to 30, ayer to 25, hoy to 20),
            mapOf(anteayer to 3.20, ayer to 3.20, hoy to 3.20)),
        ProductoData(3, "Pan Bimbo Integral 680g", 5.80,
            mapOf(anteayer to 20, ayer to 18, hoy to 22),
            mapOf(anteayer to 5.80, ayer to 5.80, hoy to 5.80)),
        ProductoData(4, "Huevos San Juan x12", 6.50,
            mapOf(anteayer to 40, ayer to 35, hoy to 30),
            mapOf(anteayer to 6.50, ayer to 6.50, hoy to 6.50)),
        ProductoData(5, "Aceite Vegetal 1L", 8.90,
            mapOf(anteayer to 25, ayer to 25, hoy to 22),
            mapOf(anteayer to 8.90, ayer to 8.90, hoy to 8.90)),
        ProductoData(6, "Azúcar Blanca 1kg", 3.40,
            mapOf(anteayer to 60, ayer to 58, hoy to 55),
            mapOf(anteayer to 3.40, ayer to 3.40, hoy to 3.40)),
        ProductoData(7, "Fideos Don Victorio 500g", 2.80,
            mapOf(anteayer to 35, ayer to 33, hoy to 30),
            mapOf(anteayer to 2.80, ayer to 2.80, hoy to 2.80)),
        ProductoData(8, "Café Nescafé 200g", 12.50,
            mapOf(anteayer to 15, ayer to 14, hoy to 10),
            mapOf(anteayer to 12.50, ayer to 12.50, hoy to 9.90)),
        ProductoData(9, "Atún Florida 170g", 4.20,
            mapOf(anteayer to 45, ayer to 40, hoy to 38),
            mapOf(anteayer to 4.20, ayer to 4.20, hoy to 4.20)),
        ProductoData(10, "Galletas Oreo 120g", 3.90,
            mapOf(anteayer to 28, ayer to 25, hoy to 20),
            mapOf(anteayer to 3.90, ayer to 3.90, hoy to 3.90))
    )

    println("Paso 1: Limpiando tablas existentes...")
    limpiarTablas()
    println("   -> OK")
    println()

    println("Paso 2: Insertando ${productos.size} productos en MinimarketDB...")
    insertarEnMinimarketDB(productos)
    println()

    println("Paso 3: Insertando ${productos.size} productos en MinimarketMirror...")
    insertarEnMirror(productos)
    println()

    println("Paso 4: Insertando ${fechas.size} fechas en Dim_Tiempo...")
    val tiempoKeys = insertarDimTiempo(fechas)
    println("   -> Keys generadas: ${tiempoKeys.values}")
    println()

    println("Paso 5: Insertando ${productos.size} productos en Dim_Articulo...")
    val articuloKeys = insertarDimArticulo(productos)
    println("   -> Keys generadas: ${articuloKeys.values}")
    println()

    println("Paso 6: Insertando datos en Fact_Inventario...")
    var totalFilas = 0
    for (prod in productos) {
        val articuloKey = articuloKeys[prod.id]!!
        for (fecha in fechas) {
            val tiempoKey = tiempoKeys[fecha]!!
            val stock = prod.stocks[fecha]!!
            val precio = prod.precios[fecha]!!
            insertarFact(tiempoKey, articuloKey, stock, precio)
            println("   Fact_Inventario: ${prod.descripcion} ($fecha) -> Stock=$stock, Precio=$precio")
            totalFilas++
        }
    }
    println("   -> Total filas insertadas: $totalFilas")
    println()

    println("Paso 7: Creando Vista_Stock_Cruzado...")
    crearVistaCrossTab(fechas)
    println()

    println("═════════════ CARGA COMPLETADA ═════════════")
    println("   Productos: ${productos.size}")
    println("   Fechas: ${fechas.size}")
    println("   Filas en Fact_Inventario: $totalFilas")
    println("   Vista: Vista_Stock_Cruzado")
    println("═════════════════════════════════════════════")
    println()
    println("Ejecute 'runViewCrossTab' para visualizar los datos.")
}

private fun limpiarTablas() {
    var conn: Connection? = null
    var stmt: java.sql.Statement? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL_DW)
        stmt = conn.createStatement()
        stmt.executeUpdate("DELETE FROM Fact_Inventario")
        stmt.executeUpdate("DELETE FROM Dim_Articulo")
        stmt.executeUpdate("DELETE FROM Dim_Tiempo")
        stmt.executeUpdate("DBCC CHECKIDENT('Dim_Articulo', RESEED, 0)")
        stmt.executeUpdate("DBCC CHECKIDENT('Dim_Tiempo', RESEED, 0)")
        stmt.close()
        conn.close()

        conn = DriverManager.getConnection(AppConfig.JDBC_URL_MIRROR)
        stmt = conn.createStatement()
        stmt.executeUpdate("DELETE FROM ArticulosMirror")
        stmt.close()
        conn.close()

        conn = DriverManager.getConnection(AppConfig.JDBC_URL)
        stmt = conn.createStatement()
        stmt.executeUpdate("DELETE FROM Articulos")
        stmt.close()
        conn.close()
    } catch (e: Exception) {
        println("   ERROR al limpiar tablas: ${e.message}")
    } finally {
        stmt?.close()
        conn?.close()
    }
}

private fun insertarEnMinimarketDB(productos: List<ProductoData>) {
    var conn: Connection? = null
    var stmt: PreparedStatement? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL)
        stmt = conn.prepareStatement(
            "INSERT INTO Articulos (ID, Descripcion, Precio, Stock) VALUES (?, ?, ?, ?)"
        )
        for (prod in productos) {
            stmt.setInt(1, prod.id)
            stmt.setString(2, prod.descripcion)
            stmt.setDouble(3, prod.precioNormal)
            stmt.setInt(4, prod.stocks[LocalDate.now()]!!)
            stmt.executeUpdate()
            println("   + Articulos: ${prod.descripcion}")
        }
    } catch (e: Exception) {
        println("   ERROR al insertar en MinimarketDB: ${e.message}")
    } finally {
        stmt?.close()
        conn?.close()
    }
}

private fun insertarEnMirror(productos: List<ProductoData>) {
    var conn: Connection? = null
    var stmt: PreparedStatement? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL_MIRROR)
        stmt = conn.prepareStatement(
            "INSERT INTO ArticulosMirror (ID, Descripcion, Precio, Stock, Activo) VALUES (?, ?, ?, ?, 1)"
        )
        for (prod in productos) {
            stmt.setInt(1, prod.id)
            stmt.setString(2, prod.descripcion)
            stmt.setDouble(3, prod.precioNormal)
            stmt.setInt(4, prod.stocks[LocalDate.now()]!!)
            stmt.executeUpdate()
            println("   + ArticulosMirror: ${prod.descripcion}")
        }
    } catch (e: Exception) {
        println("   ERROR al insertar en Mirror: ${e.message}")
    } finally {
        stmt?.close()
        conn?.close()
    }
}

private fun insertarDimTiempo(fechas: List<LocalDate>): Map<LocalDate, Int> {
    val keys = mutableMapOf<LocalDate, Int>()
    var conn: Connection? = null
    var stmt: PreparedStatement? = null
    var rs: java.sql.ResultSet? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL_DW)
        for (fecha in fechas) {
            stmt = conn.prepareStatement(
                """
                INSERT INTO Dim_Tiempo (Fecha, Anio, Mes, Dia)
                OUTPUT inserted.TiempoKey
                VALUES (?, ?, ?, ?)
                """.trimIndent()
            )
            stmt.setDate(1, java.sql.Date.valueOf(fecha))
            stmt.setInt(2, fecha.year)
            stmt.setInt(3, fecha.monthValue)
            stmt.setInt(4, fecha.dayOfMonth)
            rs = stmt.executeQuery()
            if (rs.next()) {
                keys[fecha] = rs.getInt(1)
                println("   + Dim_Tiempo: $fecha -> TiempoKey=${rs.getInt(1)}")
            }
            rs.close()
            rs = null
            stmt.close()
            stmt = null
        }
    } catch (e: Exception) {
        println("   ERROR al insertar Dim_Tiempo: ${e.message}")
    } finally {
        rs?.close()
        stmt?.close()
        conn?.close()
    }
    return keys
}

private fun insertarDimArticulo(productos: List<ProductoData>): Map<Int, Int> {
    val keys = mutableMapOf<Int, Int>()
    var conn: Connection? = null
    var stmt: PreparedStatement? = null
    var rs: java.sql.ResultSet? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL_DW)
        for (prod in productos) {
            stmt = conn.prepareStatement(
                """
                INSERT INTO Dim_Articulo (ArticuloID, Descripcion, Precio)
                OUTPUT inserted.ArticuloKey
                VALUES (?, ?, ?)
                """.trimIndent()
            )
            stmt.setInt(1, prod.id)
            stmt.setString(2, prod.descripcion)
            stmt.setDouble(3, prod.precioNormal)
            rs = stmt.executeQuery()
            if (rs.next()) {
                keys[prod.id] = rs.getInt(1)
                println("   + Dim_Articulo: ${prod.descripcion} -> ArticuloKey=${rs.getInt(1)}")
            }
            rs.close()
            rs = null
            stmt.close()
            stmt = null
        }
    } catch (e: Exception) {
        println("   ERROR al insertar Dim_Articulo: ${e.message}")
    } finally {
        rs?.close()
        stmt?.close()
        conn?.close()
    }
    return keys
}

private fun insertarFact(tiempoKey: Int, articuloKey: Int, stock: Int, precio: Double) {
    var conn: Connection? = null
    var stmt: PreparedStatement? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL_DW)
        stmt = conn.prepareStatement(
            "INSERT INTO Fact_Inventario (TiempoKey, ArticuloKey, Stock, PrecioActual) VALUES (?, ?, ?, ?)"
        )
        stmt.setInt(1, tiempoKey)
        stmt.setInt(2, articuloKey)
        stmt.setInt(3, stock)
        stmt.setDouble(4, precio)
        stmt.executeUpdate()
    } catch (e: Exception) {
        println("   ERROR al insertar Fact_Inventario: ${e.message}")
    } finally {
        stmt?.close()
        conn?.close()
    }
}

private fun crearVistaCrossTab(fechas: List<LocalDate>) {
    var conn: Connection? = null
    var stmt: java.sql.Statement? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL_DW)
        stmt = conn.createStatement()
        stmt.executeUpdate("DROP VIEW IF EXISTS Vista_Stock_Cruzado")

        val pivotCols = fechas.joinToString(", ") { "[$it]" }
        val createSQL = """
            CREATE VIEW Vista_Stock_Cruzado AS
            SELECT
                ArticuloID AS ID,
                Descripcion AS Articulo,
                $pivotCols
            FROM (
                SELECT
                    da.ArticuloID,
                    da.Descripcion,
                    dt.Fecha,
                    fi.Stock
                FROM Fact_Inventario fi
                INNER JOIN Dim_Articulo da ON fi.ArticuloKey = da.ArticuloKey
                INNER JOIN Dim_Tiempo dt ON fi.TiempoKey = dt.TiempoKey
            ) AS SourceTable
            PIVOT (
                SUM(Stock)
                FOR Fecha IN ($pivotCols)
            ) AS PivotTable
        """.trimIndent()
        stmt.executeUpdate(createSQL)
        println("   Vista_Stock_Cruzado creada con ${fechas.size} columnas de fecha.")
    } catch (e: Exception) {
        println("   ERROR al crear vista: ${e.message}")
    } finally {
        stmt?.close()
        conn?.close()
    }
}
