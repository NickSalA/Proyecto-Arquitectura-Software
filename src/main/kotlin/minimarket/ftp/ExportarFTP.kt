package minimarket.ftp

import minimarket.config.AppConfig
import minimarket.data.model.Articulo
import minimarket.data.repository.RepositorioArticulosSQL
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.Locale

fun main() {
    println("╔══════════════════════════════════════════════════╗")
    println("║          MINIMARKET POS - ExportarFTP            ║")
    println("║        Publicacion de datos operacionales        ║")
    println("╚══════════════════════════════════════════════════╝")
    println()

    val repository = RepositorioArticulosSQL()
    val articulos = repository.listar()
    println("Articulos extraidos desde MinimarketDB: ${articulos.size}")

    if (articulos.isEmpty()) {
        println("No hay articulos para exportar.")
        return
    }

    val csv = generarCsv(articulos)
    subirArchivo(csv.toByteArray(StandardCharsets.UTF_8))

    println()
    println("═════════════ EXPORTACION COMPLETADA ═════════════")
    println("   Servidor FTP: ${AppConfig.FTP_HOST}:${AppConfig.FTP_PORT}")
    println("   Archivo remoto: ${AppConfig.FTP_REMOTE_FILE}")
    println("   Articulos publicados: ${articulos.size}")
    println("═══════════════════════════════════════════════════")
}

private fun generarCsv(articulos: List<Articulo>): String {
    val builder = StringBuilder()
    builder.appendLine("ID,Descripcion,Precio,Stock")
    for (articulo in articulos) {
        builder.append(articulo.id)
        builder.append(',')
        builder.append(escapeCsv(articulo.descripcion))
        builder.append(',')
        builder.append(String.format(Locale.US, "%.2f", articulo.precio))
        builder.append(',')
        builder.append(articulo.stock)
        builder.appendLine()
    }
    return builder.toString()
}

private fun escapeCsv(value: String): String {
    val normalized = value.trim()
    val requiresQuotes = normalized.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
    if (!requiresQuotes) return normalized
    return "\"${normalized.replace("\"", "\"\"")}\""
}

private fun subirArchivo(bytes: ByteArray) {
    val ftp = FTPClient()
    try {
        ftp.connect(AppConfig.FTP_HOST, AppConfig.FTP_PORT)
        val reply = ftp.replyCode
        if (!FTPReply.isPositiveCompletion(reply)) {
            error("El servidor FTP rechazo la conexion. Codigo: $reply")
        }

        if (!ftp.login(AppConfig.FTP_USER, AppConfig.FTP_PASSWORD)) {
            error("No se pudo iniciar sesion en FTP con el usuario ${AppConfig.FTP_USER}")
        }

        ftp.enterLocalPassiveMode()
        ftp.setFileType(FTP.ASCII_FILE_TYPE)

        ByteArrayInputStream(bytes).use { input ->
            if (!ftp.storeFile(AppConfig.FTP_REMOTE_FILE, input)) {
                error("No se pudo subir ${AppConfig.FTP_REMOTE_FILE}. Respuesta FTP: ${ftp.replyString}")
            }
        }
    } finally {
        if (ftp.isConnected) {
            runCatching { ftp.logout() }
            ftp.disconnect()
        }
    }
}
