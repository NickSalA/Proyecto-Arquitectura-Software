package minimarket.mirror

import minimarket.application.AppConfig
import minimarket.data.model.Articulo
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement

fun main() {
    println("╔══════════════════════════════════════════════════╗")
    println("║        MINIMARKET POS - ActualizarMirror         ║")
    println("║          Sincronizacion desde servidor FTP       ║")
    println("╚══════════════════════════════════════════════════╝")
    println()

    val csv = descargarArchivo()
    val articulos = leerArticulos(csv)
    println("Articulos leidos desde FTP: ${articulos.size}")

    if (articulos.isEmpty()) {
        println("No hay articulos validos en el archivo FTP.")
        return
    }

    val resultado = sincronizarMirror(articulos)

    println()
    println("═════════════ MIRROR ACTUALIZADO ═════════════")
    println("   Base destino: MinimarketMirror")
    println("   Tabla destino: ArticulosMirror")
    println("   Registros sincronizados: ${resultado.sincronizados}")
    println("   Registros marcados inactivos: ${resultado.inactivos}")
    println("═══════════════════════════════════════════════")
}

private data class ResultadoMirror(
    val sincronizados: Int,
    val inactivos: Int
)

private fun descargarArchivo(): String {
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

        val output = ByteArrayOutputStream()
        if (!ftp.retrieveFile(AppConfig.FTP_REMOTE_FILE, output)) {
            error("No se pudo descargar ${AppConfig.FTP_REMOTE_FILE}. Respuesta FTP: ${ftp.replyString}")
        }

        return output.toString(StandardCharsets.UTF_8)
    } finally {
        if (ftp.isConnected) {
            runCatching { ftp.logout() }
            ftp.disconnect()
        }
    }
}

private fun leerArticulos(csv: String): List<Articulo> {
    return csv.lineSequence()
        .drop(1)
        .filter { it.isNotBlank() }
        .mapNotNull { line -> parseArticulo(line) }
        .toList()
}

private fun parseArticulo(line: String): Articulo? {
    val values = parseCsvLine(line)
    if (values.size != 4) return null
    val id = values[0].trim().toIntOrNull() ?: return null
    val descripcion = values[1].trim()
    val precio = values[2].trim().toDoubleOrNull() ?: return null
    val stock = values[3].trim().toIntOrNull() ?: return null
    if (id <= 0 || descripcion.isEmpty() || precio < 0 || stock < 0) return null
    return Articulo(id, descripcion, precio, stock)
}

private fun parseCsvLine(line: String): List<String> {
    val values = mutableListOf<String>()
    val current = StringBuilder()
    var insideQuotes = false
    var index = 0

    while (index < line.length) {
        val char = line[index]
        when {
            char == '"' && insideQuotes && index + 1 < line.length && line[index + 1] == '"' -> {
                current.append('"')
                index++
            }
            char == '"' -> insideQuotes = !insideQuotes
            char == ',' && !insideQuotes -> {
                values.add(current.toString())
                current.clear()
            }
            else -> current.append(char)
        }
        index++
    }
    values.add(current.toString())
    return values
}

private fun sincronizarMirror(articulos: List<Articulo>): ResultadoMirror {
    var conn: Connection? = null
    var stmt: PreparedStatement? = null
    try {
        conn = DriverManager.getConnection(AppConfig.JDBC_URL_MIRROR)
        conn.autoCommit = false

        val mergeSql = """
            MERGE ArticulosMirror AS destino
            USING (VALUES (?, ?, ?, ?)) AS origen (ID, Descripcion, Precio, Stock)
                ON destino.ID = origen.ID
            WHEN MATCHED THEN
                UPDATE SET
                    Descripcion = origen.Descripcion,
                    Precio = origen.Precio,
                    Stock = origen.Stock,
                    Activo = 1,
                    FechaSincronizacion = SYSUTCDATETIME()
            WHEN NOT MATCHED THEN
                INSERT (ID, Descripcion, Precio, Stock, Activo, FechaSincronizacion)
                VALUES (origen.ID, origen.Descripcion, origen.Precio, origen.Stock, 1, SYSUTCDATETIME());
        """.trimIndent()

        stmt = conn.prepareStatement(mergeSql)
        for (articulo in articulos) {
            stmt.setInt(1, articulo.id)
            stmt.setString(2, articulo.descripcion)
            stmt.setDouble(3, articulo.precio)
            stmt.setInt(4, articulo.stock)
            stmt.executeUpdate()
        }
        stmt.close()

        val inactivos = marcarInactivos(conn, articulos.map { it.id })
        conn.commit()
        return ResultadoMirror(articulos.size, inactivos)
    } catch (e: Exception) {
        conn?.rollback()
        error("Error al sincronizar Mirror: ${e.message}")
    } finally {
        stmt?.close()
        conn?.close()
    }
}

private fun marcarInactivos(conn: Connection, idsActivos: List<Int>): Int {
    if (idsActivos.isEmpty()) {
        conn.prepareStatement(
            "UPDATE ArticulosMirror SET Activo = 0, FechaSincronizacion = SYSUTCDATETIME() WHERE Activo = 1"
        ).use { stmt -> return stmt.executeUpdate() }
    }

    val placeholders = idsActivos.joinToString(",") { "?" }
    val sql = """
        UPDATE ArticulosMirror
        SET Activo = 0, FechaSincronizacion = SYSUTCDATETIME()
        WHERE Activo = 1 AND ID NOT IN ($placeholders)
    """.trimIndent()

    conn.prepareStatement(sql).use { stmt ->
        idsActivos.forEachIndexed { index, id -> stmt.setInt(index + 1, id) }
        return stmt.executeUpdate()
    }
}
