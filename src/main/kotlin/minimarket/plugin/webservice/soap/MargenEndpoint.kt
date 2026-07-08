package minimarket.plugin.webservice.soap

import minimarket.plugin.webservice.soap.model.MargenRequest
import minimarket.plugin.webservice.soap.model.MargenResponse
import minimarket.plugin.webservice.soap.service.MargenService
import org.springframework.ws.server.endpoint.annotation.Endpoint
import org.springframework.ws.server.endpoint.annotation.PayloadRoot
import org.springframework.ws.server.endpoint.annotation.RequestPayload
import org.springframework.ws.server.endpoint.annotation.ResponsePayload

@Endpoint
class MargenEndpoint(
    private val margenService: MargenService
) {
    companion object {
        private const val NAMESPACE_URI = "http://minimarket.plugin/soap/margen"
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CalcularMargenRequest")
    @ResponsePayload
    fun calcular(@RequestPayload request: MargenRequest): MargenResponse {
        return margenService.calcular(request)
    }
}
