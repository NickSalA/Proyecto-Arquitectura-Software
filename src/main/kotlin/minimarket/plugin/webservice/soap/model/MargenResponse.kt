package minimarket.plugin.webservice.soap.model

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.XmlType
import java.math.BigDecimal

@XmlRootElement(name = "CalcularMargenResponse", namespace = "http://minimarket.plugin/soap/margen")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "",
    propOrder = [
        "precioCompra", "precioVenta", "cantidad",
        "gananciaUnitaria", "gananciaTotal", "margenPorcentual", "margenSobreCosto"
    ]
)
data class MargenResponse(
    @field:XmlElement(namespace = "http://minimarket.plugin/soap/margen")
    var precioCompra: BigDecimal = BigDecimal.ZERO,

    @field:XmlElement(namespace = "http://minimarket.plugin/soap/margen")
    var precioVenta: BigDecimal = BigDecimal.ZERO,

    @field:XmlElement(namespace = "http://minimarket.plugin/soap/margen")
    var cantidad: Int = 0,

    @field:XmlElement(namespace = "http://minimarket.plugin/soap/margen")
    var gananciaUnitaria: BigDecimal = BigDecimal.ZERO,

    @field:XmlElement(namespace = "http://minimarket.plugin/soap/margen")
    var gananciaTotal: BigDecimal = BigDecimal.ZERO,

    @field:XmlElement(namespace = "http://minimarket.plugin/soap/margen")
    var margenPorcentual: BigDecimal = BigDecimal.ZERO,

    @field:XmlElement(namespace = "http://minimarket.plugin/soap/margen")
    var margenSobreCosto: BigDecimal = BigDecimal.ZERO
)
