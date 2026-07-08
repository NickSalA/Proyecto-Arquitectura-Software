package minimarket.plugin.webservice.soap.model

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement

@XmlAccessorType(XmlAccessType.FIELD)
data class ArticleDto(
    @field:XmlElement var id: Int = 0,
    @field:XmlElement var descripcion: String = "",
    @field:XmlElement var precio: Double = 0.0,
    @field:XmlElement var stock: Int = 0
)

@XmlRootElement(name = "GetAllArticlesRequest")
@XmlAccessorType(XmlAccessType.FIELD)
class GetAllArticlesRequest

@XmlRootElement(name = "GetAllArticlesResponse")
@XmlAccessorType(XmlAccessType.FIELD)
data class GetAllArticlesResponse(
    @field:XmlElement(name = "articles") var articles: List<ArticleDto>? = null
)

@XmlRootElement(name = "GetArticleByIdRequest")
@XmlAccessorType(XmlAccessType.FIELD)
data class GetArticleByIdRequest(
    @field:XmlElement var id: Int = 0
)

@XmlRootElement(name = "GetArticleByIdResponse")
@XmlAccessorType(XmlAccessType.FIELD)
data class GetArticleByIdResponse(
    @field:XmlElement(name = "article") var article: ArticleDto? = null
)

@XmlRootElement(name = "CreateArticleRequest")
@XmlAccessorType(XmlAccessType.FIELD)
data class CreateArticleRequest(
    @field:XmlElement var article: ArticleDto? = null
)

@XmlRootElement(name = "CreateArticleResponse")
@XmlAccessorType(XmlAccessType.FIELD)
data class CreateArticleResponse(
    @field:XmlElement var success: Boolean = false,
    @field:XmlElement var message: String = ""
)

@XmlRootElement(name = "UpdateArticleRequest")
@XmlAccessorType(XmlAccessType.FIELD)
data class UpdateArticleRequest(
    @field:XmlElement var article: ArticleDto? = null
)

@XmlRootElement(name = "UpdateArticleResponse")
@XmlAccessorType(XmlAccessType.FIELD)
data class UpdateArticleResponse(
    @field:XmlElement var success: Boolean = false,
    @field:XmlElement var message: String = ""
)

@XmlRootElement(name = "DeleteArticleRequest")
@XmlAccessorType(XmlAccessType.FIELD)
data class DeleteArticleRequest(
    @field:XmlElement var id: Int = 0
)

@XmlRootElement(name = "DeleteArticleResponse")
@XmlAccessorType(XmlAccessType.FIELD)
data class DeleteArticleResponse(
    @field:XmlElement var success: Boolean = false,
    @field:XmlElement var message: String = ""
)
