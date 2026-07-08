package minimarket.plugin.webservice.soap

import minimarket.data.model.Articulo
import minimarket.plugin.webservice.soap.model.*
import minimarket.service.ArticuloService
import org.springframework.ws.server.endpoint.annotation.Endpoint
import org.springframework.ws.server.endpoint.annotation.PayloadRoot
import org.springframework.ws.server.endpoint.annotation.RequestPayload
import org.springframework.ws.server.endpoint.annotation.ResponsePayload

@Endpoint
class SoapArticleEndpoint(
    private val service: ArticuloService
) {
    companion object {
        private const val NAMESPACE_URI = "http://minimarket.plugin/soap"
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetAllArticlesRequest")
    @ResponsePayload
    fun getAllArticles(@RequestPayload request: GetAllArticlesRequest): GetAllArticlesResponse {
        val articles = service.listar().map { it.toDto() }
        return GetAllArticlesResponse(articles)
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetArticleByIdRequest")
    @ResponsePayload
    fun getArticleById(@RequestPayload request: GetArticleByIdRequest): GetArticleByIdResponse {
        val article = service.buscar(request.id)
        return GetArticleByIdResponse(article?.toDto())
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "CreateArticleRequest")
    @ResponsePayload
    fun createArticle(@RequestPayload request: CreateArticleRequest): CreateArticleResponse {
        val dto = request.article
            ?: return CreateArticleResponse(false, "No se proporcionaron datos del articulo.")
        val articulo = Articulo(dto.id, dto.descripcion, dto.precio, dto.stock)
        val result = service.registrar(articulo)
        return CreateArticleResponse(result.exitoso, result.mensaje)
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "UpdateArticleRequest")
    @ResponsePayload
    fun updateArticle(@RequestPayload request: UpdateArticleRequest): UpdateArticleResponse {
        val dto = request.article
            ?: return UpdateArticleResponse(false, "No se proporcionaron datos del articulo.")
        val articulo = Articulo(dto.id, dto.descripcion, dto.precio, dto.stock)
        val result = service.actualizar(articulo)
        return UpdateArticleResponse(result.exitoso, result.mensaje)
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeleteArticleRequest")
    @ResponsePayload
    fun deleteArticle(@RequestPayload request: DeleteArticleRequest): DeleteArticleResponse {
        val result = service.eliminar(request.id)
        return DeleteArticleResponse(result.exitoso, result.mensaje)
    }
}

private fun Articulo.toDto() = ArticleDto(id, descripcion, precio, stock)
