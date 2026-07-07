package minimarket.plugin.seguridad

import minimarket.config.AppConfig
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import org.springframework.stereotype.Repository

@Repository
class RepositorioActividadSQL {

    private val connectionUrl = AppConfig.JDBC_URL

    fun upsert(operador: String, estado: String) {
        var conn: Connection? = null
        var stmt: java.sql.CallableStatement? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call dbo.sp_Actividad_Upsert(?, ?)}")
            stmt.setString(1, operador)
            stmt.setString(2, estado)
            stmt.executeUpdate()
        } catch (_: Exception) {
        } finally {
            stmt?.close()
            conn?.close()
        }
    }

    fun listar(): List<RegistroActividad> {
        val registros = mutableListOf<RegistroActividad>()
        var conn: Connection? = null
        var stmt: java.sql.CallableStatement? = null
        var rs: ResultSet? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call dbo.sp_Actividad_Listar()}")
            rs = stmt.executeQuery()
            while (rs.next()) {
                registros.add(
                    RegistroActividad(
                        id = rs.getInt("Id"),
                        operador = rs.getString("Operador"),
                        estado = rs.getString("Estado"),
                        ultimoLatido = rs.getString("UltimoLatido"),
                        inicioSesion = rs.getString("InicioSesion"),
                        finSesion = rs.getString("FinSesion")
                    )
                )
            }
        } catch (_: Exception) {
        } finally {
            rs?.close()
            stmt?.close()
            conn?.close()
        }
        return registros
    }

    fun marcarAusentes(timeoutSegundos: Int) {
        var conn: Connection? = null
        var stmt: java.sql.CallableStatement? = null
        try {
            conn = DriverManager.getConnection(connectionUrl)
            stmt = conn.prepareCall("{call dbo.sp_Actividad_MarcarAusentes(?)}")
            stmt.setInt(1, timeoutSegundos)
            stmt.executeUpdate()
        } catch (_: Exception) {
        } finally {
            stmt?.close()
            conn?.close()
        }
    }
}
