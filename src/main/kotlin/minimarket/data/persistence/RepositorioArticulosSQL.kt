package minimarket.data.persistence

import minimarket.application.AppConfig
import minimarket.data.model.Articulo
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Repositorio JDBC para la tabla transaccional Articulos de MinimarketDB.
 *
 * En el Entregable 2 el cliente Swing ya no escribe archivos .dat. Cada
 * operacion CRUD abre una conexion contra SQL Server en el servidor MATHIPC,
 * ejecuta una sentencia parametrizada y libera explicitamente ResultSet,
 * PreparedStatement y Connection dentro del bloque finally.
 */
class RepositorioArticulosSQL {

    private val connectionUrl = AppConfig.JDBC_URL

    /**
     * Inserta un articulo nuevo en la base transaccional.
     *
     * SQL ejecutado:
     * INSERT INTO Articulos (ID, Descripcion, Precio, Stock) VALUES (?, ?, ?, ?)
     *
     * Los signos ? se completan con PreparedStatement para evitar concatenar
     * datos de la interfaz y reducir riesgo de inyeccion SQL.
     */
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
     * La consulta usa ORDER BY ID para que la tabla Swing muestre un resultado
     * estable entre recargas y no dependa del orden fisico de almacenamiento.
     */
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
            println("Error al actualizar articulo: ${e.message}")
            return false
        } finally {
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
        var stmt: PreparedStatement? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareStatement("DELETE FROM Articulos WHERE ID = ?")
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
     * Verifica existencia antes de registrar o actualizar desde la interfaz.
     *
     * SELECT COUNT(*) permite responder con un booleano simple sin transferir
     * columnas innecesarias desde el servidor de base de datos.
     */
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

    /**
     * Retorna la cantidad total de articulos en la tabla transaccional.
     */
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
            println("Error al contar articulos: ${e.message}")
            return 0
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
    }
}
