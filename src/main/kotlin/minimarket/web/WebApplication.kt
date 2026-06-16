package minimarket.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["minimarket"])
class WebApplication

fun main(args: Array<String>) {
    runApplication<WebApplication>(*args)
}
