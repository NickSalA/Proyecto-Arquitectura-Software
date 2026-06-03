package minimarket.olap

import minimarket.application.AppConfig
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

fun main() {
    println("╔══════════════════════════════════════════════════╗")
    println("║        MINIMARKET POS - CreateCrossTab           ║")
    println("║          Creacion de Vista OLAP v1.0             ║")
    println("╚══════════════════════════════════════════════════╝")
    println()

    println("Recopilando informacion de fechas disponibles en Dim_Tiempo...")
    val fechas = obtenerFechas()
    if (fechas.isEmpty()) {
        println("   ERROR: No hay fechas en Dim_Tiempo.")
        println("   Ejecute GenerarDatawareHouse.kt primero.")
        return
    }
    println("   -> Fechas encontradas: ${fechas.size}")
    println()

    println("Construyendo consulta PIVOT dinamica...")
    val columnasPivot = fechas.joinToString(", ") { "[$it]" }
    val pivotQuery = """
        CREATE OR ALTER VIEW Vista_Stock_Cruzado AS
        SELECT
            da.ArticuloID AS [ID],
            da.Descripcion AS [Articulo],
            $columnasPivot
        FROM
            Fact_Inventario fi
            INNER JOIN Dim_Articulo da ON fi.ArticuloKey = da.ArticuloKey
            INNER JOIN Dim_Tiempo dt ON fi.TiempoKey = dt.TiempoKey
        GROUP BY
            da.ArticuloID, da.Descripcion
        PIVOT (
            SUM(fi.Stock)
            FOR dt.Fecha IN ($columnasPivot)
        ) AS pvt
    """.trimIndent()
    println()

    println("Ejecutando creacion de vista en MinimarketDW...")
    var conn: Connection? = null
    var stmt: java.sql.Statement? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL_DW)
        stmt = conn.createStatement()
        stmt.executeUpdate(pivotQuery)
        println()
        println("═════════════ VISTA CREADA ═════════════")
        println("   Nombre: Vista_Stock_Cruzado")
        println("   Base: Fact_Inventario + Dim_Articulo + Dim_Tiempo")
        println("   Filas: ID, Articulo (de Dim_Articulo)")
        println("   Columnas: ${fechas.size} fechas")
        println("   Valores: Stock acumulado por fecha")
        println("═════════════════════════════════════════════")
        println()
        println("Para visualizar los datos, ejecute: ViewCrossTab.kt")
    } catch (e: Exception) {
        println("   ERROR al crear vista: ${e.message}")
    } finally {
        stmt?.close()
        conn?.close()
    }
}

private fun obtenerFechas(): List<String> {
    val fechas = mutableListOf<String>()
    var conn: Connection? = null
    var stmt: PreparedStatement? = null
    var rs: ResultSet? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL_DW)
        stmt = conn.prepareStatement("SELECT CONVERT(VARCHAR, Fecha, 23) AS FechaStr FROM Dim_Tiempo ORDER BY Fecha")
        rs = stmt.executeQuery()
        while (rs.next()) {
            fechas.add(rs.getString("FechaStr"))
        }
    } catch (e: Exception) {
        println("   ERROR al obtener fechas: ${e.message}")
    } finally {
        rs?.close()
        stmt?.close()
        conn?.close()
    }
    return fechas
}