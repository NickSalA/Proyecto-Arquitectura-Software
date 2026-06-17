package minimarket.dw

import minimarket.data.repository.RepositorioDataWarehouseSQL
import java.time.LocalDate

fun main() {
    val repo = RepositorioDataWarehouseSQL()

    println("╔══════════════════════════════════════════════════╗")
    println("║       MINIMARKET POS - GenerarDatawarehouse      ║")
    println("║                 Proceso ETL v1.0                 ║")
    println("╚══════════════════════════════════════════════════╝")
    println()

    println("Paso 1: Extrayendo articulos activos desde MinimarketMirror...")
    val articulos = repo.extraerArticulosDesdeMirror()
    println("   -> Articulos encontrados: ${articulos.size}")
    println()

    if (articulos.isEmpty()) {
        println("   No hay articulos para procesar. Ejecute primero ExportarFTP y ActualizarMirror.")
        return
    }

    println("Paso 2: Insertando fecha actual en Dim_Tiempo...")
    val tiempoKey = repo.mergeDimTiempo(LocalDate.now())
    if (tiempoKey <= 0) {
        println("   ERROR: No se pudo obtener TiempoKey. El ETL se detiene.")
        return
    }
    println("   -> TiempoKey generado: $tiempoKey")
    println()

    println("Paso 3: Sincronizando Dim_Articulo con MERGE...")
    val articuloKeys = mutableListOf<Int>()
    for (articulo in articulos) {
        val key = repo.mergeDimArticulo(articulo)
        if (key > 0) articuloKeys.add(key)
    }
    if (articuloKeys.size != articulos.size) {
        println("   ERROR: No todos los articulos fueron sincronizados. El ETL se detiene.")
        return
    }
    println("   -> Articulos sincronizados: ${articuloKeys.size}")
    println()

    println("Paso 4: Cargando datos en Fact_Inventario con MERGE...")
    var insertados = 0
    for (i in articuloKeys.indices) {
        val articuloKey = articuloKeys[i]
        val articulo = articulos[i]
        val accion = repo.mergeFactInventario(tiempoKey, articuloKey, articulo.stock, articulo.precio)
        if (accion == "INSERT") insertados++
        println("   ${formatearAccion(accion)} Fact_Inventario ArticuloKey=$articuloKey [Stock=${articulo.stock}, Precio=${articulo.precio}]")
    }
    val actualizados = articuloKeys.size - insertados
    println("   -> Nuevos registros: $insertados")
    println("   -> Registros actualizados: $actualizados")
    println()

    println("═════════════ RESUMEN DEL ETL ═════════════")
    println("   Fecha procesada: ${LocalDate.now()}")
    println("   Articulos en origen: ${articulos.size}")
    println("   TiempoKey: $tiempoKey")
    println("   Total filas en Fact_Inventario: ${articuloKeys.size}")
    println("═════════════════════════════════════════════")
}

private fun formatearAccion(accion: String): String {
    return when (accion) {
        "INSERT" -> "\u271A INSERT"
        "UPDATE" -> "\u21BB UPDATE"
        else -> accion
    }
}
