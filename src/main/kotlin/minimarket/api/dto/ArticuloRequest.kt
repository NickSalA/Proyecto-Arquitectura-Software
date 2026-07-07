package minimarket.api.dto

data class ArticuloRequest(
    val id: Int? = null,
    val descripcion: String? = null,
    val precio: Double? = null,
    val stock: Int? = null
)
