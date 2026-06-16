package minimarket.web

import minimarket.application.AppConfig
import minimarket.data.model.Articulo
import minimarket.service.ArticuloService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class HomeController {
    @GetMapping("/")
    fun home(): String = "redirect:/articulos"
}

@Controller
@RequestMapping("/articulos")
class ArticuloController(
    private val service: ArticuloService
) {
    @GetMapping
    fun index(model: Model): String {
        prepararModelo(model, ArticuloForm(), null)
        return "articulos/index"
    }

    @GetMapping("/{id}/editar")
    fun editar(@PathVariable id: Int, model: Model, redirect: RedirectAttributes): String {
        val articulo = service.buscar(id)
        if (articulo == null) {
            redirect.addFlashAttribute("error", "No existe un articulo con ID $id.")
            return "redirect:/articulos"
        }

        prepararModelo(
            model,
            ArticuloForm(articulo.id, articulo.descripcion, articulo.precio, articulo.stock),
            articulo.id
        )
        return "articulos/index"
    }

    @PostMapping
    fun registrar(@ModelAttribute form: ArticuloForm, redirect: RedirectAttributes): String {
        val articulo = form.toArticulo()
        if (articulo == null) {
            redirect.addFlashAttribute("error", "Complete todos los campos con valores validos.")
            return "redirect:/articulos"
        }

        val resultado = service.registrar(articulo)
        redirect.addFlashAttribute(if (resultado.exitoso) "success" else "error", resultado.mensaje)
        return "redirect:/articulos"
    }

    @PostMapping("/{id}")
    fun actualizar(
        @PathVariable id: Int,
        @ModelAttribute form: ArticuloForm,
        redirect: RedirectAttributes
    ): String {
        val articulo = form.toArticulo(id)
        if (articulo == null) {
            redirect.addFlashAttribute("error", "Complete todos los campos con valores validos.")
            return "redirect:/articulos/$id/editar"
        }

        val resultado = service.actualizar(articulo)
        redirect.addFlashAttribute(if (resultado.exitoso) "success" else "error", resultado.mensaje)
        return "redirect:/articulos"
    }

    @PostMapping("/{id}/eliminar")
    fun eliminar(@PathVariable id: Int, redirect: RedirectAttributes): String {
        val resultado = service.eliminar(id)
        redirect.addFlashAttribute(if (resultado.exitoso) "success" else "error", resultado.mensaje)
        return "redirect:/articulos"
    }

    private fun prepararModelo(model: Model, form: ArticuloForm, editId: Int?) {
        model.addAttribute("articulos", service.listar())
        model.addAttribute("articuloForm", form)
        model.addAttribute("editId", editId)
        model.addAttribute("dbDisplay", AppConfig.DB_DISPLAY)
        model.addAttribute("ftpDisplay", "${AppConfig.FTP_HOST}:${AppConfig.FTP_PORT}")
    }
}

data class ArticuloForm(
    var id: Int? = null,
    var descripcion: String = "",
    var precio: Double? = null,
    var stock: Int? = null
) {
    fun toArticulo(forcedId: Int? = null): Articulo? {
        val safeId = forcedId ?: id ?: return null
        val safePrecio = precio ?: return null
        val safeStock = stock ?: return null
        return Articulo(safeId, descripcion.trim(), safePrecio, safeStock)
    }
}
