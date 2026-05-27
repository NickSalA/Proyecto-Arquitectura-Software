package minimarket.application

import minimarket.data.model.Articulo
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager

/**
 * Componente de Consolidación (Update.EXE)
 *
 * Lee articulos.dat desde la carpeta compartida de red y sincroniza
 * los registros hacia SQL Server usando lógica IF EXISTS.
 *
 * Conexión JDBC hacia el SQL Server levantado por Docker.
 */

fun main() {
    println("╔══════════════════════════════════════════════════╗")
    println("║      MINIMARKET POS - Componente UPDATE          ║")
    println("║        Consolidación SQL Server v1.0             ║")
    println("╚══════════════════════════════════════════════════╝")
    println()

    val sourcePath = Paths.get(AppConfig.sharedDataPath)

    val sourceFile = sourcePath.toFile()
    if (!sourceFile.exists()) {
        println("   ✗ ERROR: No se encontró el archivo en la carpeta compartida.")
        println("   Ruta esperada: $sourcePath")
        println("   Ejecute el componente SEND primero.")
        return
    }

    println("   Archivo de red encontrado: $sourcePath")
    println("   Tamaño: ${sourceFile.length()} bytes")
    println()

    val articulos = leerArticulosDesdeArchivo(sourceFile)
    println("   Registros válidos leídos: ${articulos.size}")
    println()

    println("   Conectando a SQL Server...")
    var connection: Connection? = null
    try {
        connection = DriverManager.getConnection(AppConfig.JDBC_URL)
        println("   ✓ Conexión establecida.")
        println()

        var insertados = 0
        var actualizados = 0

        for (articulo in articulos) {
            val existed = sincronizarArticulo(connection, articulo)
            if (existed) actualizados++ else insertados++
        }

        val eliminados = eliminarArticulosObsoletos(connection, articulos.map { it.id })

        println()
        println("   ═══ RESUMEN DE SINCRONIZACIÓN ═══")
        println("   • Insertados:   $insertados")
        println("   • Actualizados: $actualizados")
        println("   • Eliminados:   $eliminados")
        println("   • Total cambios:${insertados + actualizados + eliminados}")
        println("   ✓ Consolidación completada exitosamente.")
    } catch (e: Exception) {
        println("   ✗ ERROR de conexión a SQL Server: ${e.message}")
    } finally {
        connection?.close()
    }
}

private fun leerArticulosDesdeArchivo(file: File): List<Articulo> {
    val articulos = mutableListOf<Articulo>()
    val raf = RandomAccessFile(file, "r")
    try {
        val fileLength = raf.length()
        var offset: Long = 0
        while (offset < fileLength) {
            raf.seek(offset)
            val articulo = Articulo.readFromFile(raf)
            if (!articulo.isDeleted()) {
                articulos.add(articulo)
            }
            offset += Articulo.RECORD_SIZE
        }
    } finally {
        raf.close()
    }
    return articulos
}

private fun sincronizarArticulo(conn: Connection, articulo: Articulo): Boolean {
    val checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM Articulos WHERE ID = ?")
    checkStmt.setInt(1, articulo.id)
    val rs = checkStmt.executeQuery()
    rs.next()
    val exists = rs.getInt(1) > 0
    rs.close()
    checkStmt.close()

    if (exists) {
        val stmt = conn.prepareStatement(
            "UPDATE Articulos SET Descripcion = ?, Precio = ?, Stock = ? WHERE ID = ?"
        )
        stmt.setString(1, articulo.descripcion)
        stmt.setDouble(2, articulo.precio)
        stmt.setInt(3, articulo.stock)
        stmt.setInt(4, articulo.id)
        stmt.executeUpdate()
        stmt.close()
        print("   ↻ UPDATE ID=${articulo.id}")
    } else {
        val stmt = conn.prepareStatement(
            "INSERT INTO Articulos (ID, Descripcion, Precio, Stock) VALUES (?, ?, ?, ?)"
        )
        stmt.setInt(1, articulo.id)
        stmt.setString(2, articulo.descripcion)
        stmt.setDouble(3, articulo.precio)
        stmt.setInt(4, articulo.stock)
        stmt.executeUpdate()
        stmt.close()
        print("   ✚ INSERT ID=${articulo.id}")
    }
    println(" [${articulo.descripcion.trim()}]")
    return exists
}

private fun eliminarArticulosObsoletos(conn: Connection, idsActivos: List<Int>): Int {
    if (idsActivos.isEmpty()) {
        val stmt = conn.prepareStatement("DELETE FROM Articulos")
        try {
            return stmt.executeUpdate()
        } finally {
            stmt.close()
        }
    }

    val placeholders = idsActivos.joinToString(",") { "?" }
    val stmt = conn.prepareStatement("DELETE FROM Articulos WHERE ID NOT IN ($placeholders)")
    try {
        idsActivos.forEachIndexed { index, id -> stmt.setInt(index + 1, id) }
        return stmt.executeUpdate()
    } finally {
        stmt.close()
    }
}
