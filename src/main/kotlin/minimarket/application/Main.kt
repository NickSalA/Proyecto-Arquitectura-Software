package minimarket.application

import minimarket.data.model.Articulo
import minimarket.data.persistence.ArchivoArticulos
import java.util.Scanner

/**
 * Aplicación principal del Sistema de Gestión de Inventario Local.
 *
 * Proporciona un menú interactivo por consola para realizar operaciones
 * CRUD sobre el inventario almacenado en un archivo binario de acceso
 * aleatorio (arquitectura unitaria con servidor de datos).
 */

/** Ruta del archivo de datos local. */
const val DATA_PATH = "data/articulos.dat"

fun main() {
    val archivo = ArchivoArticulos(DATA_PATH)
    val scanner = Scanner(System.`in`)

    println("╔══════════════════════════════════════════════════╗")
    println("║     MINIMARKET POS - Sistema de Inventario       ║")
    println("║          Arquitectura Unitaria v1.0              ║")
    println("╚══════════════════════════════════════════════════╝")

    var running = true

    while (running) {
        println()
        println("┌──────────────── MENÚ PRINCIPAL ────────────────┐")
        println("│  1. Registrar Artículo                         │")
        println("│  2. Buscar Artículo por ID                     │")
        println("│  3. Listar Artículos                           │")
        println("│  4. Actualizar Artículo                        │")
        println("│  5. Eliminar Artículo                          │")
        println("│  6. Salir                                      │")
        println("└────────────────────────────────────────────────┘")
        print("   Seleccione una opción: ")

        when (scanner.nextLine().trim()) {
            "1" -> registrarArticulo(archivo, scanner)
            "2" -> buscarArticulo(archivo, scanner)
            "3" -> listarArticulos(archivo)
            "4" -> actualizarArticulo(archivo, scanner)
            "5" -> eliminarArticulo(archivo, scanner)
            "6" -> {
                running = false
                println("\n   ✓ Sistema cerrado correctamente.")
                println("   Ejecute Send para transferir los datos al servidor.\n")
            }
            else -> println("\n   ✗ Opción no válida. Intente de nuevo.")
        }
    }
}

// =============================================================================
// FUNCIONES DE CADA OPERACIÓN
// =============================================================================

/**
 * Solicita los datos de un nuevo artículo y lo registra en el archivo.
 */
private fun registrarArticulo(archivo: ArchivoArticulos, scanner: Scanner) {
    println("\n   ── REGISTRO DE ARTÍCULO ──")

    print("   ID (entero): ")
    val id = scanner.nextLine().trim().toIntOrNull()
    if (id == null || id <= 0) {
        println("   ✗ ID inválido. Debe ser un entero positivo.")
        return
    }

    if (archivo.existe(id)) {
        println("   ✗ Ya existe un artículo con ID $id.")
        return
    }

    print("   Descripción (máx 20 caracteres): ")
    val descripcion = scanner.nextLine().trim()
    if (descripcion.isEmpty()) {
        println("   ✗ La descripción no puede estar vacía.")
        return
    }

    print("   Precio: ")
    val precio = scanner.nextLine().trim().toDoubleOrNull()
    if (precio == null || precio < 0) {
        println("   ✗ Precio inválido. Debe ser un número positivo.")
        return
    }

    print("   Stock: ")
    val stock = scanner.nextLine().trim().toIntOrNull()
    if (stock == null || stock < 0) {
        println("   ✗ Stock inválido. Debe ser un entero no negativo.")
        return
    }

    val articulo = Articulo(id, descripcion, precio, stock)
    if (archivo.agregar(articulo)) {
        println("   ✓ Artículo registrado exitosamente.")
    } else {
        println("   ✗ Error al registrar el artículo.")
    }
}

/**
 * Busca un artículo por ID y muestra sus datos.
 */
private fun buscarArticulo(archivo: ArchivoArticulos, scanner: Scanner) {
    println("\n   ── BÚSQUEDA DE ARTÍCULO ──")

    print("   Ingrese ID a buscar: ")
    val id = scanner.nextLine().trim().toIntOrNull()
    if (id == null) {
        println("   ✗ ID inválido.")
        return
    }

    val articulo = archivo.buscar(id)
    if (articulo != null) {
        imprimirTabla(listOf(articulo))
    } else {
        println("   ✗ No se encontró artículo con ID $id.")
    }
}

/**
 * Lista todos los artículos activos en formato de tabla.
 */
private fun listarArticulos(archivo: ArchivoArticulos) {
    println("\n   ── LISTADO DE ARTÍCULOS ──")

    val articulos = archivo.listar()
    if (articulos.isEmpty()) {
        println("   (No hay artículos registrados)")
        return
    }

    imprimirTabla(articulos)
    println("   Total: ${articulos.size} artículo(s)")
}

/**
 * Actualiza los campos de un artículo existente.
 */
private fun actualizarArticulo(archivo: ArchivoArticulos, scanner: Scanner) {
    println("\n   ── ACTUALIZACIÓN DE ARTÍCULO ──")

    print("   Ingrese ID del artículo a actualizar: ")
    val id = scanner.nextLine().trim().toIntOrNull()
    if (id == null) {
        println("   ✗ ID inválido.")
        return
    }

    val actual = archivo.buscar(id)
    if (actual == null) {
        println("   ✗ No se encontró artículo con ID $id.")
        return
    }

    println("   Datos actuales:")
    imprimirTabla(listOf(actual))

    print("   Nueva descripción [${actual.descripcion}]: ")
    val descInput = scanner.nextLine().trim()
    val descripcion = if (descInput.isEmpty()) actual.descripcion else descInput

    print("   Nuevo precio [${actual.precio}]: ")
    val precioInput = scanner.nextLine().trim()
    val precio = if (precioInput.isEmpty()) actual.precio else {
        precioInput.toDoubleOrNull() ?: run {
            println("   ✗ Precio inválido.")
            return
        }
    }

    print("   Nuevo stock [${actual.stock}]: ")
    val stockInput = scanner.nextLine().trim()
    val stock = if (stockInput.isEmpty()) actual.stock else {
        stockInput.toIntOrNull() ?: run {
            println("   ✗ Stock inválido.")
            return
        }
    }

    val actualizado = Articulo(id, descripcion, precio, stock)
    if (archivo.actualizar(actualizado)) {
        println("   ✓ Artículo actualizado exitosamente.")
    } else {
        println("   ✗ Error al actualizar el artículo.")
    }
}

/**
 * Elimina lógicamente un artículo (marca ID como -1).
 */
private fun eliminarArticulo(archivo: ArchivoArticulos, scanner: Scanner) {
    println("\n   ── ELIMINACIÓN DE ARTÍCULO ──")

    print("   Ingrese ID del artículo a eliminar: ")
    val id = scanner.nextLine().trim().toIntOrNull()
    if (id == null) {
        println("   ✗ ID inválido.")
        return
    }

    val articulo = archivo.buscar(id)
    if (articulo == null) {
        println("   ✗ No se encontró artículo con ID $id.")
        return
    }

    println("   Artículo a eliminar:")
    imprimirTabla(listOf(articulo))

    print("   ¿Confirmar eliminación? (S/N): ")
    val confirmacion = scanner.nextLine().trim().uppercase()
    if (confirmacion != "S") {
        println("   Operación cancelada.")
        return
    }

    if (archivo.eliminar(id)) {
        println("   ✓ Artículo eliminado exitosamente (eliminación lógica).")
    } else {
        println("   ✗ Error al eliminar el artículo.")
    }
}

// =============================================================================
// UTILIDADES DE PRESENTACIÓN
// =============================================================================

/**
 * Imprime una lista de artículos en formato de tabla con bordes.
 */
private fun imprimirTabla(articulos: List<Articulo>) {
    println("   +------+----------------------+------------+--------+")
    println("   | ID   | Descripción          |     Precio |  Stock |")
    println("   +------+----------------------+------------+--------+")
    for (art in articulos) {
        println("   $art")
    }
    println("   +------+----------------------+------------+--------+")
}
