package minimarket.api.controller

import minimarket.api.dto.ArticuloRequest
import minimarket.data.model.Articulo
import minimarket.service.ArticuloService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/articulos")
class ApiArticuloController(
    private val service: ArticuloService
) {
    @GetMapping
    fun listar(): List<Articulo> = service.listar()

    @GetMapping("/{id}")
    fun buscar(@PathVariable id: Int): ResponseEntity<Any> {
        val articulo = service.buscar(id)
        return if (articulo != null) {
            ResponseEntity.ok(articulo)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "No existe un articulo con ID $id."))
        }
    }

    @PostMapping
    fun registrar(@RequestBody body: ArticuloRequest): ResponseEntity<Any> {
        val id = body.id ?: return ResponseEntity.badRequest()
            .body(mapOf("error" to "El campo 'id' es obligatorio."))
        val descripcion = body.descripcion?.trim()
            ?: return ResponseEntity.badRequest()
                .body(mapOf("error" to "El campo 'descripcion' es obligatorio."))
        val precio = body.precio
            ?: return ResponseEntity.badRequest()
                .body(mapOf("error" to "El campo 'precio' es obligatorio."))
        val stock = body.stock
            ?: return ResponseEntity.badRequest()
                .body(mapOf("error" to "El campo 'stock' es obligatorio."))

        val resultado = service.registrar(Articulo(id, descripcion, precio, stock))
        return if (resultado.exitoso) {
            ResponseEntity.status(HttpStatus.CREATED)
                .body(mapOf("mensaje" to resultado.mensaje))
        } else {
            ResponseEntity.badRequest()
                .body(mapOf("error" to resultado.mensaje))
        }
    }

    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable id: Int,
        @RequestBody body: ArticuloRequest
    ): ResponseEntity<Any> {
        val descripcion = body.descripcion?.trim()
            ?: return ResponseEntity.badRequest()
                .body(mapOf("error" to "El campo 'descripcion' es obligatorio."))
        val precio = body.precio
            ?: return ResponseEntity.badRequest()
                .body(mapOf("error" to "El campo 'precio' es obligatorio."))
        val stock = body.stock
            ?: return ResponseEntity.badRequest()
                .body(mapOf("error" to "El campo 'stock' es obligatorio."))

        val resultado = service.actualizar(Articulo(id, descripcion, precio, stock))
        return if (resultado.exitoso) {
            ResponseEntity.ok(mapOf("mensaje" to resultado.mensaje))
        } else {
            if (resultado.mensaje.contains("No existe")) {
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(mapOf("error" to resultado.mensaje))
            } else {
                ResponseEntity.badRequest()
                    .body(mapOf("error" to resultado.mensaje))
            }
        }
    }

    @DeleteMapping("/{id}")
    fun eliminar(@PathVariable id: Int): ResponseEntity<Any> {
        val resultado = service.eliminar(id)
        return if (resultado.exitoso) {
            ResponseEntity.ok(mapOf("mensaje" to resultado.mensaje))
        } else {
            if (resultado.mensaje.contains("No existe")) {
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(mapOf("error" to resultado.mensaje))
            } else {
                ResponseEntity.badRequest()
                    .body(mapOf("error" to resultado.mensaje))
            }
        }
    }
}
