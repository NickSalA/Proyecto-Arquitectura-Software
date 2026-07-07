package minimarket.plugin.seguridad

import minimarket.config.AppConfig
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/actividad")
class ActividadController(
    private val service: ActividadService
) {
    data class HeartbeatRequest(
        val operador: String,
        val estado: String
    )

    @PostMapping("/heartbeat")
    fun heartbeat(@RequestBody body: HeartbeatRequest): Map<String, Any> {
        val operador = body.operador.ifBlank { AppConfig.SECURITY_OPERATOR }
        service.registrarLatido(operador, body.estado)
        return mapOf("ok" to true)
    }
}
