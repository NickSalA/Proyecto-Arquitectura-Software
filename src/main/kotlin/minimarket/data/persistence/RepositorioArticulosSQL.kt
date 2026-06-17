package minimarket.data.persistence

import minimarket.application.AppConfig
import minimarket.data.model.Articulo
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

/**
 * Repositorio JDBC para la tabla transaccional Articulos de MinimarketDB.
 *
 * En el Entregable 2 todas las operaciones CRUD se delegan a procedimientos
 * almacenados en SQL Server. La capa de aplicacion solo invoca los SPs
 * mediante CallableStatement; no existen sentencias DML escritas en Kotlin.
 */
class RepositorioArticulosSQL {

    private val connectionUrl = AppConfig.JDBC_URL

    /**
     * Inserta un articulo nuevo via [sp_AgregarArticulo].
     */
    fun agregar(articulo: Articulo): Boolean {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call sp_AgregarArticulo(?, ?, ?, ?)}")
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

    /**
     * Busca un articulo por ID via [sp_BuscarArticulo].
     */
    fun buscar(id: Int): Articulo? {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call sp_BuscarArticulo(?)}")
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

    /**
     * Lista todos los articulos via [sp_ListarArticulos].
     */
    fun listar(): List<Articulo> {
        val articulos = mutableListOf<Articulo>()
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call sp_ListarArticulos}")
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

    /**
     * Actualiza los campos de un articulo via [sp_ActualizarArticulo].
     */
    fun actualizar(articulo: Articulo): Boolean {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call sp_ActualizarArticulo(?, ?, ?, ?)}")
            stmt.setInt(1, articulo.id)
            stmt.setString(2, articulo.descripcion)
            stmt.setDouble(3, articulo.precio)
            stmt.setInt(4, articulo.stock)
            val filas = stmt.executeUpdate()
            return filas > 0
        } catch (e: Exception) {
            println("Error al actualizar articulo: ${e.message}")
            return false
        } finally {
            stmt?.close()
            conn?.close()
        }
    }

    /**
     * Elimina un articulo por ID via [sp_EliminarArticulo].
     */
    fun eliminar(id: Int): Boolean {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call sp_EliminarArticulo(?)}")
            stmt.setInt(1, id)
            val filas = stmt.executeUpdate()
            return filas > 0
        } catch (e: Exception) {
            println("Error al eliminar articulo: ${e.message}")
            return false
        } finally {
            stmt?.close()
            conn?.close()
        }
    }

    /**
     * Verifica existencia de un articulo via [sp_ExisteArticulo].
     */
    fun existe(id: Int): Boolean {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call sp_ExisteArticulo(?)}")
            stmt.setInt(1, id)
            rs = stmt.executeQuery()
            rs.next()
            return rs.getInt("Cantidad") > 0
        } catch (e: Exception) {
            println("Error al verificar existencia: ${e.message}")
            return false
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
    }

    /**
     * Retorna la cantidad total de articulos via [sp_ContarArticulos].
     */
    fun cantidad(): Int {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call sp_ContarArticulos}")
            rs = stmt.executeQuery()
            rs.next()
            return rs.getInt("Cantidad")
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
