package minimarket.dw

import minimarket.data.repository.RepositorioDataWarehouseSQL

fun main() {
    val repo = RepositorioDataWarehouseSQL()

    println("╔══════════════════════════════════════════════════╗")
    println("║       MINIMARKET POS - ViewCrossTab              ║")
    println("║          Consulta OLAP PIVOT v1.0                ║")
    println("╚══════════════════════════════════════════════════╝")
    println()

    println("Conectando a MinimarketDW en modo solo lectura...")
    val (headers, rows) = repo.consultarVistaCrossTab()
    println("   -> Conexion establecida.")
    println()

    if (headers.isEmpty()) {
        println("   ERROR: No se pudo obtener datos de Vista_Stock_Cruzado.")
        println()
        println("   Asegurese de que:")
        println("   1. MinimarketDW exista y este accesible")
        println("   2. GenerarDatawareHouse.kt haya sido ejecutado")
        println("   3. CreateCrossTab.kt haya sido ejecutado")
        return
    }

    val columnCount = headers.size
    println("Ejecutando consulta a Vista_Stock_Cruzado...")
    println()
    println("═════════════ RESULTADO CROSSTAB ═════════════")
    println()

    val colWidths = IntArray(columnCount) { i ->
        val headerLen = headers[i].length
        val dataLen = rows.maxOfOrNull { row -> row[i]?.length ?: 0 } ?: headerLen
        maxOf(headerLen, dataLen)
    }

    val separator = "\u250C" + colWidths.map { "\u2500".repeat(it + 2) }.joinToString("\u252C") + "\u2510"
    val headerRow = "\u2502" + headers.mapIndexed { i, h -> " ${h.padEnd(colWidths[i])} " }.joinToString("\u2502") + "\u2502"
    val headerSeparator = "\u251C" + colWidths.map { "\u2500".repeat(it + 2) }.joinToString("\u253C") + "\u2524"

    println(separator)
    println(headerRow)
    println(headerSeparator)

    for (row in rows) {
        val dataRow = "\u2502" + row.mapIndexed { i, v -> " ${(v ?: "-").padEnd(colWidths[i])} " }.joinToString("\u2502") + "\u2502"
        println(dataRow)
    }

    val bottomSeparator = "\u2514" + colWidths.map { "\u2500".repeat(it + 2) }.joinToString("\u2534") + "\u2518"
    println(bottomSeparator)

    println()
    println("═══════════════════════════════════════════════")
    println("   Total filas: ${rows.size}")
    println("   Total columnas: ${columnCount - 2} (fechas de analisis)")
    println("   Vista: Vista_Stock_Cruzado")
    println("═══════════════════════════════════════════════")
}
