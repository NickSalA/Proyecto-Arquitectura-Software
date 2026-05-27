package minimarket.application

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * Componente de Transferencia (Send.EXE)
 *
 * Copia articulos.dat a una carpeta compartida de red.
 * Usa la ruta UNC configurada para el primer entregable.
 */

fun main() {
    println("╔══════════════════════════════════════════════════╗")
    println("║       MINIMARKET POS - Componente SEND           ║")
    println("║          Transferencia de Datos v1.0             ║")
    println("╚══════════════════════════════════════════════════╝")
    println()

    val archivoLocal = File(AppConfig.LOCAL_DATA_PATH)
    if (!archivoLocal.exists()) {
        println("   ✗ ERROR: No se encontró el archivo de datos local.")
        println("   Ruta esperada: ${archivoLocal.absolutePath}")
        println("   Ejecute la aplicación principal primero.")
        return
    }

    val fileSize = archivoLocal.length()
    val recordCount = fileSize / 56
    println("   Archivo local encontrado:")
    println("   • Ruta:      ${archivoLocal.absolutePath}")
    println("   • Tamaño:    $fileSize bytes")
    println("   • Registros: $recordCount")
    println()

    val destPath = Paths.get(AppConfig.sharedDataPath)

    println("   Destino: $destPath")
    println()

    try {
        val destDir = destPath.parent
        if (destDir != null && !Files.exists(destDir)) {
            Files.createDirectories(destDir)
        }
        Files.copy(archivoLocal.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING)
        println("   ✓ Transferencia completada exitosamente.")
        println("   • Tamaño verificado: ${destPath.toFile().length()} bytes")
    } catch (e: Exception) {
        println("   ✗ ERROR durante la transferencia: ${e.message}")
    }
}
