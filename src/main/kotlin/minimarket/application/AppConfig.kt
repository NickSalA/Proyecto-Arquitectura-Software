package minimarket.application

object AppConfig {
    const val LOCAL_DATA_PATH = "data/articulos.dat"
    const val DEFAULT_SHARED_DATA_PATH = "\\\\MATHIPC\\Users\\User\\Desktop\\DATOS\\articulos.dat"
    const val JDBC_URL = "jdbc:sqlserver://localhost:1433;databaseName=MinimarketDB;user=sa;password=DreamTeam_26;trustServerCertificate=true"
    const val JDBC_URL_DW = "jdbc:sqlserver://localhost:1433;databaseName=MinimarketDW;user=sa;password=DreamTeam_26;trustServerCertificate=true"

    val sharedDataPath: String = System.getenv("SHARED_DATA_PATH")?.takeIf { it.isNotBlank() }
        ?: DEFAULT_SHARED_DATA_PATH
}
