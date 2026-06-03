package minimarket.application

object AppConfig {
    private val dbHost = System.getenv("DB_HOST")?.takeIf { it.isNotBlank() } ?: "MATHIPC"
    private val dbPort = System.getenv("DB_PORT")?.takeIf { it.isNotBlank() } ?: "1433"
    private val dbUser = System.getenv("DB_USER")?.takeIf { it.isNotBlank() } ?: "sa"
    private val dbPassword = System.getenv("DB_PASSWORD")?.takeIf { it.isNotBlank() } ?: "DreamTeam_26"

    val DB_DISPLAY = "$dbHost:$dbPort"
    val JDBC_URL = buildJdbcUrl("MinimarketDB")
    val JDBC_URL_DW = buildJdbcUrl("MinimarketDW")

    private fun buildJdbcUrl(databaseName: String): String {
        return "jdbc:sqlserver://$dbHost:$dbPort;databaseName=$databaseName;user=$dbUser;password=$dbPassword;trustServerCertificate=true"
    }
}
