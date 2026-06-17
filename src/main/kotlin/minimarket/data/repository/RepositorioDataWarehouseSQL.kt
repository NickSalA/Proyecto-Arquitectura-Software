package minimarket.data.repository

import minimarket.config.AppConfig
import minimarket.data.model.Articulo
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.time.LocalDate

class RepositorioDataWarehouseSQL {

    private val dwUrl = AppConfig.JDBC_URL_DW
    private val mirrorUrl = AppConfig.JDBC_URL_MIRROR
    private val dbUrl = AppConfig.JDBC_URL

    fun limpiarTablasDW() {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        try {
            conn = DriverManager.getConnection(dwUrl)
            stmt = conn.prepareCall("{call dbo.sp_DW_LimpiarTablas}")
            stmt.executeUpdate()
        } catch (e: Exception) {
            println("   ERROR al limpiar DW: ${e.message}")
        } finally {
            stmt?.close()
            conn?.close()
        }
    }

    fun limpiarMirror() {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        try {
            conn = DriverManager.getConnection(mirrorUrl)
            stmt = conn.prepareCall("{call dbo.sp_Mirror_LimpiarTablas}")
            stmt.executeUpdate()
        } catch (e: Exception) {
            println("   ERROR al limpiar Mirror: ${e.message}")
        } finally {
            stmt?.close()
            conn?.close()
        }
    }

    fun limpiarMinimarketDB() {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        try {
            conn = DriverManager.getConnection(dbUrl)
            stmt = conn.prepareCall("{call dbo.sp_Articulo_LimpiarTablas}")
            stmt.executeUpdate()
        } catch (e: Exception) {
            println("   ERROR al limpiar MinimarketDB: ${e.message}")
        } finally {
            stmt?.close()
            conn?.close()
        }
    }

    fun insertarEnMinimarketDB(articulo: Articulo) {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        try {
            conn = DriverManager.getConnection(dbUrl)
            stmt = conn.prepareCall("{call dbo.sp_Articulo_Insertar(?, ?, ?, ?)}")
            stmt.setInt(1, articulo.id)
            stmt.setString(2, articulo.descripcion)
            stmt.setDouble(3, articulo.precio)
            stmt.setInt(4, articulo.stock)
            stmt.executeUpdate()
        } catch (e: Exception) {
            println("   ERROR al insertar en MinimarketDB: ${e.message}")
        } finally {
            stmt?.close()
            conn?.close()
        }
    }

    fun insertarEnMirror(articulo: Articulo) {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        try {
            conn = DriverManager.getConnection(mirrorUrl)
            stmt = conn.prepareCall("{call dbo.sp_Mirror_Insertar(?, ?, ?, ?)}")
            stmt.setInt(1, articulo.id)
            stmt.setString(2, articulo.descripcion)
            stmt.setDouble(3, articulo.precio)
            stmt.setInt(4, articulo.stock)
            stmt.executeUpdate()
        } catch (e: Exception) {
            println("   ERROR al insertar en Mirror: ${e.message}")
        } finally {
            stmt?.close()
            conn?.close()
        }
    }

    fun insertarDimTiempo(fecha: LocalDate): Int {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(dwUrl)
            stmt = conn.prepareCall("{call dbo.sp_DW_InsertarDimTiempo(?, ?, ?, ?)}")
            stmt.setDate(1, java.sql.Date.valueOf(fecha))
            stmt.setInt(2, fecha.year)
            stmt.setInt(3, fecha.monthValue)
            stmt.setInt(4, fecha.dayOfMonth)
            rs = stmt.executeQuery()
            if (rs.next()) {
                return rs.getInt(1)
            }
            return -1
        } catch (e: Exception) {
            println("   ERROR al insertar Dim_Tiempo: ${e.message}")
            return -1
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
    }

    fun insertarDimArticulo(articuloID: Int, descripcion: String, precio: Double): Int {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(dwUrl)
            stmt = conn.prepareCall("{call dbo.sp_DW_InsertarDimArticulo(?, ?, ?)}")
            stmt.setInt(1, articuloID)
            stmt.setString(2, descripcion)
            stmt.setDouble(3, precio)
            rs = stmt.executeQuery()
            if (rs.next()) {
                return rs.getInt(1)
            }
            return -1
        } catch (e: Exception) {
            println("   ERROR al insertar Dim_Articulo: ${e.message}")
            return -1
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
    }

    fun insertarFact(tiempoKey: Int, articuloKey: Int, stock: Int, precio: Double) {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        try {
            conn = DriverManager.getConnection(dwUrl)
            stmt = conn.prepareCall("{call dbo.sp_DW_InsertarFact(?, ?, ?, ?)}")
            stmt.setInt(1, tiempoKey)
            stmt.setInt(2, articuloKey)
            stmt.setInt(3, stock)
            stmt.setDouble(4, precio)
            stmt.executeUpdate()
        } catch (e: Exception) {
            println("   ERROR al insertar Fact_Inventario: ${e.message}")
        } finally {
            stmt?.close()
            conn?.close()
        }
    }

    fun obtenerFechas(): List<String> {
        val fechas = mutableListOf<String>()
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(dwUrl)
            stmt = conn.prepareCall("{call dbo.sp_DW_ObtenerFechas}")
            rs = stmt.executeQuery()
            while (rs.next()) {
                fechas.add(rs.getString("FechaStr"))
            }
        } catch (e: Exception) {
            println("   ERROR al obtener fechas: ${e.message}")
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
        return fechas
    }

    fun mergeDimTiempo(fecha: LocalDate): Int {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(dwUrl)
            stmt = conn.prepareCall("{call dbo.sp_DW_MergeDimTiempo(?)}")
            stmt.setDate(1, java.sql.Date.valueOf(fecha))
            rs = stmt.executeQuery()
            if (rs.next()) {
                return rs.getInt("TiempoKey")
            }
            return -1
        } catch (e: Exception) {
            println("   ERROR en MergeDimTiempo: ${e.message}")
            return -1
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
    }

    fun mergeDimArticulo(articulo: Articulo): Int {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(dwUrl)
            stmt = conn.prepareCall("{call dbo.sp_DW_MergeDimArticulo(?, ?, ?)}")
            stmt.setInt(1, articulo.id)
            stmt.setString(2, articulo.descripcion)
            stmt.setDouble(3, articulo.precio)
            rs = stmt.executeQuery()
            if (rs.next()) {
                val accion = rs.getString("Accion")
                val articuloKey = rs.getInt("ArticuloKey")
                print("   ${formatearAccionMerge(accion)} Dim_Articulo ArticuloID=${articulo.id}")
                println(" [ArticuloKey=$articuloKey, ${articulo.descripcion.trim()}]")
                return articuloKey
            }
            return -1
        } catch (e: Exception) {
            println("   ERROR en MergeDimArticulo: ${e.message}")
            return -1
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
    }

    fun mergeFactInventario(tiempoKey: Int, articuloKey: Int, stock: Int, precio: Double): String {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(dwUrl)
            stmt = conn.prepareCall("{call dbo.sp_DW_MergeFactInventario(?, ?, ?, ?)}")
            stmt.setInt(1, tiempoKey)
            stmt.setInt(2, articuloKey)
            stmt.setInt(3, stock)
            stmt.setDouble(4, precio)
            rs = stmt.executeQuery()
            if (rs.next()) {
                return rs.getString("Accion")
            }
            return ""
        } catch (e: Exception) {
            println("   ERROR en MergeFactInventario: ${e.message}")
            return ""
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
    }

    fun extraerArticulosDesdeMirror(): List<Articulo> {
        val articulos = mutableListOf<Articulo>()
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(mirrorUrl)
            stmt = conn.prepareCall("{call dbo.sp_Mirror_ExtraerActivos}")
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
            println("   ERROR al extraer articulos de Mirror: ${e.message}")
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
        return articulos
    }

    fun crearVistaCrossTab() {
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        try {
            conn = DriverManager.getConnection(dwUrl)
            stmt = conn.prepareCall("{call dbo.sp_DW_CrearVistaCrossTab}")
            stmt.executeUpdate()
        } catch (e: Exception) {
            println("   ERROR al crear vista cross tab: ${e.message}")
        } finally {
            stmt?.close()
            conn?.close()
        }
    }

    fun consultarVistaCrossTab(): Pair<List<String>, List<List<String?>>> {
        val headers = mutableListOf<String>()
        val rows = mutableListOf<List<String?>>()
        var conn: Connection? = null
        var stmt: CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(dwUrl)
            stmt = conn.prepareCall("{call dbo.sp_DW_ConsultarVistaCrossTab}")
            rs = stmt.executeQuery()
            val metaData = rs.metaData
            val columnCount = metaData.columnCount
            for (i in 1..columnCount) {
                headers.add(metaData.getColumnLabel(i))
            }
            while (rs.next()) {
                val row = mutableListOf<String?>()
                for (i in 1..columnCount) {
                    row.add(rs.getString(i))
                }
                rows.add(row)
            }
        } catch (e: Exception) {
            println("   ERROR al consultar vista cross tab: ${e.message}")
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
        return Pair(headers, rows)
    }

    private fun formatearAccionMerge(accion: String): String {
        return when (accion) {
            "INSERT" -> "\u271A INSERT"
            "UPDATE" -> "\u21BB UPDATE"
            else -> accion
        }
    }
}
