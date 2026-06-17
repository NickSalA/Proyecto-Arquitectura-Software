package minimarket.dw

import minimarket.data.model.Articulo
import minimarket.data.repository.RepositorioDataWarehouseSQL
import java.time.LocalDate

data class ProductoData(
    val id: Int,
    val descripcion: String,
    val precioNormal: Double,
    val stocks: Map<LocalDate, Int>,
    val precios: Map<LocalDate, Double>
)

fun main() {
    val repo = RepositorioDataWarehouseSQL()

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
    repo.limpiarTablasDW()
    repo.limpiarMirror()
    repo.limpiarMinimarketDB()
    println("   -> OK")
    println()

    println("Paso 2: Insertando ${productos.size} productos en MinimarketDB...")
    for (prod in productos) {
        repo.insertarEnMinimarketDB(Articulo(prod.id, prod.descripcion, prod.precioNormal, prod.stocks[hoy]!!))
        println("   + Articulos: ${prod.descripcion}")
    }
    println()

    println("Paso 3: Insertando ${productos.size} productos en MinimarketMirror...")
    for (prod in productos) {
        repo.insertarEnMirror(Articulo(prod.id, prod.descripcion, prod.precioNormal, prod.stocks[hoy]!!))
        println("   + ArticulosMirror: ${prod.descripcion}")
    }
    println()

    println("Paso 4: Insertando ${fechas.size} fechas en Dim_Tiempo...")
    val tiempoKeys = mutableMapOf<LocalDate, Int>()
    for (fecha in fechas) {
        val key = repo.insertarDimTiempo(fecha)
        tiempoKeys[fecha] = key
        println("   + Dim_Tiempo: $fecha -> TiempoKey=$key")
    }
    println()

    println("Paso 5: Insertando ${productos.size} productos en Dim_Articulo...")
    val articuloKeys = mutableMapOf<Int, Int>()
    for (prod in productos) {
        val key = repo.insertarDimArticulo(prod.id, prod.descripcion, prod.precioNormal)
        articuloKeys[prod.id] = key
        println("   + Dim_Articulo: ${prod.descripcion} -> ArticuloKey=$key")
    }
    println()

    println("Paso 6: Insertando datos en Fact_Inventario...")
    var totalFilas = 0
    for (prod in productos) {
        val articuloKey = articuloKeys[prod.id]!!
        for (fecha in fechas) {
            val tiempoKey = tiempoKeys[fecha]!!
            val stock = prod.stocks[fecha]!!
            val precio = prod.precios[fecha]!!
            repo.insertarFact(tiempoKey, articuloKey, stock, precio)
            println("   Fact_Inventario: ${prod.descripcion} ($fecha) -> Stock=$stock, Precio=$precio")
            totalFilas++
        }
    }
    println("   -> Total filas insertadas: $totalFilas")
    println()

    println("Paso 7: Creando Vista_Stock_Cruzado...")
    repo.crearVistaCrossTab()
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
