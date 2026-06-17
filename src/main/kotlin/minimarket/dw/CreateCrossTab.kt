package minimarket.dw

import minimarket.data.repository.RepositorioDataWarehouseSQL

fun main() {
    val repo = RepositorioDataWarehouseSQL()

    println("╔══════════════════════════════════════════════════╗")
    println("║        MINIMARKET POS - CreateCrossTab           ║")
    println("║          Creacion de Vista OLAP v1.0             ║")
    println("╚══════════════════════════════════════════════════╝")
    println()

    println("Recopilando informacion de fechas disponibles en Dim_Tiempo...")
    val fechas = repo.obtenerFechas()
    if (fechas.isEmpty()) {
        println("   ERROR: No hay fechas en Dim_Tiempo.")
        println("   Ejecute GenerarDatawareHouse.kt primero.")
        return
    }
    println("   -> Fechas encontradas: ${fechas.size}")
    println()

    println("Construyendo y ejecutando vista PIVOT dinamica...")
    repo.crearVistaCrossTab()

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
}
