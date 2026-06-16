package minimarket.data.persistence

import minimarket.application.AppConfig
import minimarket.data.model.Articulo
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import org.springframework.stereotype.Repository

/**
 * Repositorio JDBC para la tabla transaccional Articulos de MinimarketDB.
 *
 * En el Entregable 3 la aplicacion Web MVC delega el CRUD a procedimientos
 * almacenados en SQL Server. El repositorio solo prepara llamadas JDBC y libera
 * explicitamente ResultSet, CallableStatement y Connection dentro de finally.
 */
@Repository
class RepositorioArticulosSQL {

    private val connectionUrl = AppConfig.JDBC_URL

    /**
     * Inserta un articulo nuevo en la base transaccional.
     *
     * Procedimiento ejecutado: dbo.sp_Articulo_Insertar.
     */
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

    /**
     * Busca un articulo por su ID de negocio.
     *
     * El ResultSet se transforma manualmente al modelo Articulo usado por la
     * capa de aplicacion. Si no existe una fila, se retorna null.
     */
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

    /**
     * Lista todos los articulos activos registrados en SQL Server.
     *
     * El procedimiento devuelve un resultado estable ordenado por ID.
     */
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

    /**
     * Actualiza los campos editables de un articulo existente.
     *
     * El ID no se modifica porque actua como clave primaria de la tabla. El
     * valor retornado depende de las filas afectadas por SQL Server.
     */
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

    /**
     * Elimina una fila de Articulos por ID.
     *
     * En esta arquitectura cliente/servidor la operacion ocurre directamente
     * en SQL Server; ya no existe una marca diferida en archivos locales .dat.
     */
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

    /**
     * Verifica existencia antes de registrar o actualizar desde la interfaz.
     *
     * El procedimiento devuelve un conteo simple sin transferir columnas
     * innecesarias desde el servidor de base de datos.
     */
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

    /**
     * Retorna la cantidad total de articulos en la tabla transaccional.
     */
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
