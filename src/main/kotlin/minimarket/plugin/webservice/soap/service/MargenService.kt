package minimarket.plugin.webservice.soap.service

import minimarket.plugin.webservice.soap.model.MargenRequest
import minimarket.plugin.webservice.soap.model.MargenResponse
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class MargenService {

    fun calcular(request: MargenRequest): MargenResponse {
        val precioCompra = request.precioCompra
        val precioVenta = request.precioVenta
        val cantidad = request.cantidad

        if (precioCompra < BigDecimal.ZERO || precioVenta < BigDecimal.ZERO) {
            throw IllegalArgumentException("Los precios no pueden ser negativos.")
        }
        if (cantidad <= 0) {
            throw IllegalArgumentException("La cantidad debe ser mayor a cero.")
        }

        val gananciaUnitaria = precioVenta.subtract(precioCompra)
            .setScale(2, RoundingMode.HALF_UP)

        val gananciaTotal = gananciaUnitaria.multiply(cantidad.toBigDecimal())
            .setScale(2, RoundingMode.HALF_UP)

        val margenPorcentual = if (precioVenta > BigDecimal.ZERO) {
            gananciaUnitaria.divide(precioVenta, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
        } else BigDecimal.ZERO.setScale(2)

        val margenSobreCosto = if (precioCompra > BigDecimal.ZERO) {
            gananciaUnitaria.divide(precioCompra, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
        } else BigDecimal.ZERO.setScale(2)

        return MargenResponse(
            precioCompra = precioCompra.setScale(2, RoundingMode.HALF_UP),
            precioVenta = precioVenta.setScale(2, RoundingMode.HALF_UP),
            cantidad = cantidad,
            gananciaUnitaria = gananciaUnitaria,
            gananciaTotal = gananciaTotal,
            margenPorcentual = margenPorcentual,
            margenSobreCosto = margenSobreCosto
        )
    }
}
