package minimarket.application

import java.io.File
import java.net.InetAddress
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * Componente de Transferencia (Send.EXE)
 *
 * Copia articulos.dat a una carpeta compartida de red.
 * Usa InetAddress.getLocalHost().hostName para resolver la ruta dinámica.
 */

private const val LOCAL_DATA_PATH = "data/articulos.dat"
private const val SHARED_FOLDER_NAME = "DATOS"

fun main() {
    println("╔══════════════════════════════════════════════════╗")
    println("║       MINIMARKET POS - Componente SEND          ║")
    println("║          Transferencia de Datos v1.0            ║")
    println("╚══════════════════════════════════════════════════╝")
    println()

    val archivoLocal = File(LOCAL_DATA_PATH)
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

    val hostname = try {
        InetAddress.getLocalHost().hostName
    } catch (e: Exception) { "localhost" }
    println("   Host detectado: $hostname")

    val isWindows = System.getProperty("os.name").lowercase().contains("win")
    val destPath = if (isWindows) {
        Paths.get("\\\\$hostname\\$SHARED_FOLDER_NAME\\articulos.dat")
    } else {
        Paths.get("shared", SHARED_FOLDER_NAME, "articulos.dat")
    }

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
