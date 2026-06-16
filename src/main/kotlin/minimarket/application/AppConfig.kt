package minimarket.application

object AppConfig {
    private val dbHost = System.getenv("DB_HOST")?.takeIf { it.isNotBlank() } ?: "MATHIPC"
    private val dbPort = System.getenv("DB_PORT")?.takeIf { it.isNotBlank() } ?: "1433"
    private val dbUser = System.getenv("DB_USER")?.takeIf { it.isNotBlank() } ?: "sa"
    private val dbPassword = System.getenv("DB_PASSWORD")?.takeIf { it.isNotBlank() } ?: "DreamTeam_26"

    val FTP_HOST = System.getenv("FTP_HOST")?.takeIf { it.isNotBlank() } ?: "localhost"
    val FTP_PORT = System.getenv("FTP_PORT")?.toIntOrNull() ?: 21
    val FTP_USER = System.getenv("FTP_USER")?.takeIf { it.isNotBlank() } ?: "minimarket"
    val FTP_PASSWORD = System.getenv("FTP_PASSWORD")?.takeIf { it.isNotBlank() } ?: "minimarket123"
    val FTP_REMOTE_FILE = System.getenv("FTP_REMOTE_FILE")?.takeIf { it.isNotBlank() } ?: "/articulos.csv"

    val DB_DISPLAY = "$dbHost:$dbPort"
    val JDBC_URL = buildJdbcUrl("MinimarketDB")
    val JDBC_URL_MIRROR = buildJdbcUrl("MinimarketMirror")
    val JDBC_URL_DW = buildJdbcUrl("MinimarketDW")

    private fun buildJdbcUrl(databaseName: String): String {
        return "jdbc:sqlserver://$dbHost:$dbPort;databaseName=$databaseName;user=$dbUser;password=$dbPassword;trustServerCertificate=true"
    }
}
