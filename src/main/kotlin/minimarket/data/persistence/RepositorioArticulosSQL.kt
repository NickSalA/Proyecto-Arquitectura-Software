package minimarket.data.persistence

import minimarket.application.AppConfig
import minimarket.data.model.Articulo
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class RepositorioArticulosSQL {

    private val connectionUrl = AppConfig.JDBC_URL

    fun agregar(articulo: Articulo): Boolean {
        var conn: Connection? = null
        var stmt: PreparedStatement? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareStatement(
                "INSERT INTO Articulos (ID, Descripcion, Precio, Stock) VALUES (?, ?, ?, ?)"
            )
            stmt.setInt(1, articulo.id)
            stmt.setString(2, articulo.descripcion)
            stmt.setDouble(3, articulo.precio)
            stmt.setInt(4, articulo.stock)
            stmt.executeUpdate()
            return true
        } catch (e: Exception) {
            println("Error al agregar artículo: ${e.message}")
            return false
        } finally {
            stmt?.close()
            conn?.close()
        }
    }

    fun buscar(id: Int): Articulo? {
        var conn: Connection? = null
        var stmt: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareStatement("SELECT ID, Descripcion, Precio, Stock FROM Articulos WHERE ID = ?")
            stmt.setInt(1, id)
            rs = stmt.executeQuery()
            if (rs.next()) {
                return Articulo(
                    id = rs.getInt("ID"),
                    descripcion = rs.getString("Descripcion"),
                    precio = rs.getDouble("Precio"),
                    stock = rs.getInt("Stock")
                )
            }
            return null
        } catch (e: Exception) {
            println("Error al buscar artículo: ${e.message}")
            return null
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
    }

    fun listar(): List<Articulo> {
        val articulos = mutableListOf<Articulo>()
        var conn: Connection? = null
        var stmt: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareStatement("SELECT ID, Descripcion, Precio, Stock FROM Articulos ORDER BY ID")
            rs = stmt.executeQuery()
            while (rs.next()) {
                articulos.add(
                    Articulo(
                        id = rs.getInt("ID"),
                        descripcion = rs.getString("Descripcion"),
                        precio = rs.getDouble("Precio"),
                        stock = rs.getInt("Stock")
                    )
                )
            }
        } catch (e: Exception) {
            println("Error al listar artículos: ${e.message}")
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
        return articulos
    }

    fun actualizar(articulo: Articulo): Boolean {
        var conn: Connection? = null
        var stmt: PreparedStatement? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareStatement(
                "UPDATE Articulos SET Descripcion = ?, Precio = ?, Stock = ? WHERE ID = ?"
            )
            stmt.setString(1, articulo.descripcion)
            stmt.setDouble(2, articulo.precio)
            stmt.setInt(3, articulo.stock)
            stmt.setInt(4, articulo.id)
            val filas = stmt.executeUpdate()
            return filas > 0
        } catch (e: Exception) {
            println("Error al actualizar artículo: ${e.message}")
            return false
        } finally {
            stmt?.close()
            conn?.close()
        }
    }

    fun eliminar(id: Int): Boolean {
        var conn: Connection? = null
        var stmt: PreparedStatement? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareStatement("DELETE FROM Articulos WHERE ID = ?")
            stmt.setInt(1, id)
            val filas = stmt.executeUpdate()
            return filas > 0
        } catch (e: Exception) {
            println("Error al eliminar artículo: ${e.message}")
            return false
        } finally {
            stmt?.close()
            conn?.close()
        }
    }

    fun existe(id: Int): Boolean {
        var conn: Connection? = null
        var stmt: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM Articulos WHERE ID = ?")
            stmt.setInt(1, id)
            rs = stmt.executeQuery()
            rs.next()
            return rs.getInt(1) > 0
        } catch (e: Exception) {
            println("Error al verificar existencia: ${e.message}")
            return false
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
    }

    fun cantidad(): Int {
        var conn: Connection? = null
        var stmt: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM Articulos")
            rs = stmt.executeQuery()
            rs.next()
            return rs.getInt(1)
        } catch (e: Exception) {
            println("Error al contar artículos: ${e.message}")
            return 0
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
    }
}