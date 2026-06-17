package minimarket.data.model

data class ArticuloForm(
    var id: Int? = null,
    var descripcion: String = "",
    var precio: Double? = null,
    var stock: Int? = null
) {
    fun toArticulo(forcedId: Int? = null): Articulo? {
        val safeId = forcedId ?: id ?: return null
        val safePrecio = precio ?: return null
        val safeStock = stock ?: return null
        return Articulo(safeId, descripcion.trim(), safePrecio, safeStock)
    }
}
