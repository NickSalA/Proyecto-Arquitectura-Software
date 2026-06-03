package minimarket.data.model

/**
 * Entidad inmutable que representa un artículo del inventario.
 *
 * En el Entregable 2 este modelo se transporta entre la interfaz Swing, el
 * repositorio JDBC y el ETL. La persistencia ya no se realiza en archivos .dat,
 * sino en SQL Server mediante RepositorioArticulosSQL.
 */
data class Articulo(
    val id: Int,
    val descripcion: String,
    val precio: Double,
    val stock: Int
) {
    override fun toString(): String {
        return "| %-4d | %-20s | %10.2f | %6d |".format(id, descripcion, precio, stock)
    }
}
