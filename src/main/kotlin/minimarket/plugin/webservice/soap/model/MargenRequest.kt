package minimarket.plugin.webservice.soap.model

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.bind.annotation.XmlType
import java.math.BigDecimal

@XmlRootElement(name = "CalcularMargenRequest", namespace = "http://minimarket.plugin/soap/margen")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = ["precioCompra", "precioVenta", "cantidad"])
data class MargenRequest(
    @field:XmlElement(namespace = "http://minimarket.plugin/soap/margen", required = true)
    var precioCompra: BigDecimal = BigDecimal.ZERO,

    @field:XmlElement(namespace = "http://minimarket.plugin/soap/margen", required = true)
    var precioVenta: BigDecimal = BigDecimal.ZERO,

    @field:XmlElement(namespace = "http://minimarket.plugin/soap/margen", required = true)
    var cantidad: Int = 1
)
