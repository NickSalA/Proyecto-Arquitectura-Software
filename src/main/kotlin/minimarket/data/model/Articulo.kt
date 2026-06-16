package minimarket.data.model

/**
 * Entidad inmutable que representa un artículo del inventario.
 *
 * En el Entregable 3 este modelo se transporta entre la vista MVC, los servicios,
 * el repositorio JDBC, la exportacion FTP, el Mirror y el ETL del DataWarehouse.
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
