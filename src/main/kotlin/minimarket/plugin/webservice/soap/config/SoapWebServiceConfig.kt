package minimarket.plugin.webservice.soap.config

import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.web.context.WebApplicationContext
import org.springframework.ws.config.annotation.EnableWs
import org.springframework.ws.config.annotation.WsConfigurerAdapter
import org.springframework.ws.transport.http.MessageDispatcherServlet
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition
import org.springframework.xml.xsd.SimpleXsdSchema

@EnableWs
@Configuration
class SoapWebServiceConfig : WsConfigurerAdapter() {

    @Bean
    fun messageDispatcherServlet(context: WebApplicationContext): ServletRegistrationBean<MessageDispatcherServlet> {
        val servlet = MessageDispatcherServlet(context)
        servlet.setTransformWsdlLocations(true)
        return ServletRegistrationBean(servlet, "/ws/*")
    }

    @Bean(name = ["articles"])
    fun defaultWsdl11Definition(articlesSchema: SimpleXsdSchema): DefaultWsdl11Definition {
        val definition = DefaultWsdl11Definition()
        definition.setPortTypeName("ArticlesPort")
        definition.setLocationUri("/ws")
        definition.setTargetNamespace("http://minimarket.plugin/soap")
        definition.setSchema(articlesSchema)
        return definition
    }

    @Bean
    fun articlesSchema(): SimpleXsdSchema {
        return SimpleXsdSchema(ClassPathResource("/ws/articles.xsd"))
    }

    @Bean(name = ["margen"])
    fun margenWsdl11Definition(margenSchema: SimpleXsdSchema): DefaultWsdl11Definition {
        val definition = DefaultWsdl11Definition()
        definition.setPortTypeName("MargenPort")
        definition.setLocationUri("/ws")
        definition.setTargetNamespace("http://minimarket.plugin/soap/margen")
        definition.setSchema(margenSchema)
        return definition
    }

    @Bean
    fun margenSchema(): SimpleXsdSchema {
        return SimpleXsdSchema(ClassPathResource("/ws/margen.xsd"))
    }

    @Bean
    fun jaxb2Marshaller(): Jaxb2Marshaller {
        val marshaller = Jaxb2Marshaller()
        marshaller.contextPath = "minimarket.plugin.webservice.soap.model"
        return marshaller
    }
}
