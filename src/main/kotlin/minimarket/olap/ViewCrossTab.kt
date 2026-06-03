package minimarket.olap

import minimarket.application.AppConfig
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.ResultSetMetaData

fun main() {
    println("╔══════════════════════════════════════════════════╗")
    println("║       MINIMARKET POS - ViewCrossTab              ║")
    println("║          Consulta OLAP PIVOT v1.0                ║")
    println("╚══════════════════════════════════════════════════╝")
    println()

    println("Conectando a MinimarketDW en modo solo lectura...")
    var conn: Connection? = null
    var stmt: PreparedStatement? = null
    var rs: ResultSet? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL_DW)
        println("   -> Conexion establecida.")
        println()

        println("Ejecutando consulta a Vista_Stock_Cruzado...")
        stmt = conn.prepareStatement("SELECT * FROM Vista_Stock_Cruzado ORDER BY ID")
        rs = stmt.executeQuery()
        val metaData: ResultSetMetaData = rs.metaData
        val columnCount = metaData.columnCount

        val headers = mutableListOf<String>()
        for (i in 1..columnCount) {
            headers.add(metaData.getColumnLabel(i))
        }

        val rows = mutableListOf<List<String?>>()
        while (rs.next()) {
            val row = mutableListOf<String?>()
            for (i in 1..columnCount) {
                val value = rs.getString(i)
                row.add(value)
            }
            rows.add(row)
        }

        println()
        println("═════════════ RESULTADO CROSSTAB ═════════════")
        println()

        val colWidths = IntArray(columnCount) { i ->
            val headerLen = headers[i].length
            val dataLen = rows.maxOfOrNull { row -> row[i]?.length ?: 0 } ?: headerLen
            maxOf(headerLen, dataLen)
        }

        val separator = "┌" + colWidths.map { "─".repeat(it + 2) }.joinToString("┬") + "┐"
        val headerRow = "│" + headers.mapIndexed { i, h -> " ${h.padEnd(colWidths[i])} " }.joinToString("│") + "│"
        val headerSeparator = "├" + colWidths.map { "─".repeat(it + 2) }.joinToString("┼") + "┤"

        println(separator)
        println(headerRow)
        println(headerSeparator)

        for (row in rows) {
            val dataRow = "│" + row.mapIndexed { i, v -> " ${(v ?: "-").padEnd(colWidths[i])} " }.joinToString("│") + "│"
            println(dataRow)
        }

        val bottomSeparator = "└" + colWidths.map { "─".repeat(it + 2) }.joinToString("┴") + "┘"
        println(bottomSeparator)

        println()
        println("═══════════════════════════════════════════════")
        println("   Total filas: ${rows.size}")
        println("   Total columnas: ${columnCount - 2} (fechas de analisis)")
        println("   Vista: Vista_Stock_Cruzado")
        println("═══════════════════════════════════════════════")

    } catch (e: Exception) {
        println("   ERROR: ${e.message}")
        println()
        println("   Asegurese de que:")
        println("   1. MinimarketDW exista y este accesible")
        println("   2. GenerarDatawareHouse.kt haya sido ejecutado")
        println("   3. CreateCrossTab.kt haya sido ejecutado")
    } finally {
        rs?.close()
        stmt?.close()
        conn?.close()
    }
}