package minimarket.api.controller

import minimarket.plugin.webservice.soap.model.MargenRequest
import minimarket.plugin.webservice.soap.service.MargenService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/api/margen")
class MargenController(
    private val margenService: MargenService
) {
    @GetMapping
    fun calcular(
        @RequestParam precioCompra: BigDecimal,
        @RequestParam precioVenta: BigDecimal,
        @RequestParam(defaultValue = "1") cantidad: Int
    ): ResponseEntity<Any> {
        return try {
            val response = margenService.calcular(MargenRequest(precioCompra, precioVenta, cantidad))
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}
