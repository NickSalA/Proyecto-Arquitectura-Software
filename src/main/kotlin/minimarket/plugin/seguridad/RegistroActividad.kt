package minimarket.plugin.seguridad

data class RegistroActividad(
    val id: Int = 0,
    val operador: String,
    val estado: String,
    val ultimoLatido: String,
    val inicioSesion: String? = null,
    val finSesion: String? = null
)
