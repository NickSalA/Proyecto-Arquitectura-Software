package minimarket.config

import java.io.FileInputStream
import java.util.Properties

object AppConfig {
    private val props = Properties().apply {
        try {
            load(FileInputStream("config.properties"))
        } catch (_: Exception) {
        }
    }

    private fun getProp(key: String, envName: String): String {
        val deProp = props.getProperty(key)?.takeIf { it.isNotBlank() }
        val deEnv = System.getenv(envName)?.takeIf { it.isNotBlank() }
        return deProp ?: deEnv ?: error("Configuracion faltante: $key o variable $envName. Definala en config.properties o como variable de entorno.")
    }

    private val dbHost = getProp("db.host", "DB_HOST")
    private val dbPort = getProp("db.port", "DB_PORT")
    private val dbUser = getProp("db.user", "DB_USER")
    private val dbPassword = getProp("db.password", "DB_PASSWORD")

    val FTP_HOST = getProp("ftp.host", "FTP_HOST")
    val FTP_PORT = getProp("ftp.port", "FTP_PORT").toIntOrNull() ?: error("ftp.port debe ser un numero entero.")
    val FTP_USER = getProp("ftp.user", "FTP_USER")
    val FTP_PASSWORD = getProp("ftp.password", "FTP_PASSWORD")
    val FTP_REMOTE_FILE = getProp("ftp.remote.file", "FTP_REMOTE_FILE")

    val DB_DISPLAY = "$dbHost:$dbPort"
    val JDBC_URL = buildJdbcUrl("MinimarketDB")
    val JDBC_URL_MIRROR = buildJdbcUrl("MinimarketMirror")
    val JDBC_URL_DW = buildJdbcUrl("MinimarketDW")

    private fun buildJdbcUrl(databaseName: String): String {
        return "jdbc:sqlserver://$dbHost:$dbPort;databaseName=$databaseName;user=$dbUser;password=$dbPassword;trustServerCertificate=true"
    }
}
