package minimarket.service

import minimarket.data.model.Articulo
import minimarket.data.repository.RepositorioArticulosSQL
import org.springframework.stereotype.Service

@Service
class ArticuloService(
    private val repository: RepositorioArticulosSQL
) {
    fun listar(): List<Articulo> = repository.listar()

    fun buscar(id: Int): Articulo? = repository.buscar(id)

    fun registrar(articulo: Articulo): ResultadoOperacion {
        val validation = validar(articulo)
        if (validation != null) return ResultadoOperacion(false, validation)
        if (repository.existe(articulo.id)) {
            return ResultadoOperacion(false, "Ya existe un articulo con ID ${articulo.id}.")
        }
        return if (repository.agregar(articulo)) {
            ResultadoOperacion(true, "Articulo ${articulo.id} registrado correctamente.")
        } else {
            ResultadoOperacion(false, "No se pudo registrar el articulo.")
        }
    }

    fun actualizar(articulo: Articulo): ResultadoOperacion {
        val validation = validar(articulo)
        if (validation != null) return ResultadoOperacion(false, validation)
        if (!repository.existe(articulo.id)) {
            return ResultadoOperacion(false, "No existe un articulo con ID ${articulo.id}.")
        }
        return if (repository.actualizar(articulo)) {
            ResultadoOperacion(true, "Articulo ${articulo.id} actualizado correctamente.")
        } else {
            ResultadoOperacion(false, "No se pudo actualizar el articulo.")
        }
    }

    fun eliminar(id: Int): ResultadoOperacion {
        if (id <= 0) return ResultadoOperacion(false, "El ID debe ser positivo.")
        if (!repository.existe(id)) return ResultadoOperacion(false, "No existe un articulo con ID $id.")
        return if (repository.eliminar(id)) {
            ResultadoOperacion(true, "Articulo $id eliminado correctamente.")
        } else {
            ResultadoOperacion(false, "No se pudo eliminar el articulo.")
        }
    }

    private fun validar(articulo: Articulo): String? {
        if (articulo.id <= 0) return "El ID debe ser un entero positivo."
        if (articulo.descripcion.isBlank()) return "La descripcion no puede estar vacia."
        if (articulo.precio < 0) return "El precio no puede ser negativo."
        if (articulo.stock < 0) return "El stock no puede ser negativo."
        return null
    }
}
