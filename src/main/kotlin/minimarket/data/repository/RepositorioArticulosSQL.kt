package minimarket.data.repository

import minimarket.config.AppConfig
import minimarket.data.model.Articulo
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import org.springframework.stereotype.Repository

@Repository
class RepositorioArticulosSQL {

    private val connectionUrl = AppConfig.JDBC_URL

    fun agregar(articulo: Articulo): Boolean {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call dbo.sp_Articulo_Insertar(?, ?, ?, ?)}")
            stmt.setInt(1, articulo.id)
            stmt.setString(2, articulo.descripcion)
            stmt.setDouble(3, articulo.precio)
            stmt.setInt(4, articulo.stock)
            stmt.executeUpdate()
            return true
        } catch (e: Exception) {
            println("Error al agregar articulo: ${e.message}")
            return false
        } finally {
            stmt?.close()
            conn?.close()
        }
    }

    fun buscar(id: Int): Articulo? {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call dbo.sp_Articulo_Buscar(?)}")
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
            println("Error al buscar articulo: ${e.message}")
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
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call dbo.sp_Articulo_Listar()}")
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
            println("Error al listar articulos: ${e.message}")
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
        return articulos
    }

    fun actualizar(articulo: Articulo): Boolean {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call dbo.sp_Articulo_Actualizar(?, ?, ?, ?)}")
            stmt.setInt(1, articulo.id)
            stmt.setString(2, articulo.descripcion)
            stmt.setDouble(3, articulo.precio)
            stmt.setInt(4, articulo.stock)
            rs = stmt.executeQuery()
            return rs.next() && rs.getInt("FilasAfectadas") > 0
        } catch (e: Exception) {
            println("Error al actualizar articulo: ${e.message}")
            return false
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
    }

    fun eliminar(id: Int): Boolean {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call dbo.sp_Articulo_Eliminar(?)}")
            stmt.setInt(1, id)
            rs = stmt.executeQuery()
            return rs.next() && rs.getInt("FilasAfectadas") > 0
        } catch (e: Exception) {
            println("Error al eliminar articulo: ${e.message}")
            return false
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
    }

    fun existe(id: Int): Boolean {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call dbo.sp_Articulo_Existe(?)}")
            stmt.setInt(1, id)
            rs = stmt.executeQuery()
            rs.next()
            return rs.getInt("Total") > 0
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
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call dbo.sp_Articulo_Cantidad()}")
            rs = stmt.executeQuery()
            rs.next()
            return rs.getInt("Total")
        } catch (e: Exception) {
            println("Error al contar articulos: ${e.message}")
            return 0
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
    }
}
