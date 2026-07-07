package minimarket.plugin.seguridad

import minimarket.config.AppConfig
import org.springframework.stereotype.Service

@Service
class ActividadService(
    private val repository: RepositorioActividadSQL
) {
    fun registrarLatido(operador: String, estado: String) {
        repository.upsert(operador, estado)
        repository.marcarAusentes(AppConfig.SECURITY_INACTIVITY_TIMEOUT)
    }
}
