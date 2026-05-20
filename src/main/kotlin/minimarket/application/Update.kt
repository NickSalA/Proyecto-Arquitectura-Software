package minimarket.application

import minimarket.data.model.Articulo
import java.io.File
import java.io.RandomAccessFile
import java.net.InetAddress
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager

/**
 * Componente de Consolidación (Update.EXE)
 *
 * Lee articulos.dat desde la carpeta compartida de red y sincroniza
 * los registros hacia SQL Server usando lógica IF EXISTS (MERGE).
 *
 * Conexión JDBC con autenticación integrada de Windows.
 */

private const val SHARED_FOLDER_NAME = "DATOS"
private const val DB_NAME = "MinimarketDB"

fun main() {
    println("╔══════════════════════════════════════════════════╗")
    println("║      MINIMARKET POS - Componente UPDATE         ║")
    println("║        Consolidación SQL Server v1.0            ║")
    println("╚══════════════════════════════════════════════════╝")
    println()

    val hostname = try {
        InetAddress.getLocalHost().hostName
    } catch (e: Exception) { "localhost" }

    val isWindows = System.getProperty("os.name").lowercase().contains("win")
    val sourcePath = if (isWindows) {
        Paths.get("\\\\$hostname\\$SHARED_FOLDER_NAME\\articulos.dat")
    } else {
        Paths.get("shared", SHARED_FOLDER_NAME, "articulos.dat")
    }

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

    if (articulos.isEmpty()) {
        println("   No hay registros para sincronizar.")
        return
    }

    val jdbcUrl = if (isWindows) {
        "jdbc:sqlserver://localhost;databaseName=$DB_NAME;integratedSecurity=true;trustServerCertificate=true"
    } else {
        "jdbc:sqlserver://localhost;databaseName=$DB_NAME;user=sa;password=Password123;trustServerCertificate=true"
    }

    println("   Conectando a SQL Server...")
    var connection: Connection? = null
    try {
        connection = DriverManager.getConnection(jdbcUrl)
        println("   ✓ Conexión establecida.")
        println()

        var insertados = 0
        var actualizados = 0

        for (articulo in articulos) {
            val existed = sincronizarArticulo(connection, articulo)
            if (existed) actualizados++ else insertados++
        }

        println()
        println("   ═══ RESUMEN DE SINCRONIZACIÓN ═══")
        println("   • Insertados:   $insertados")
        println("   • Actualizados: $actualizados")
        println("   • Total:        ${insertados + actualizados}")
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
