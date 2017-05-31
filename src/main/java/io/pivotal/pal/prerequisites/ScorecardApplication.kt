package io.pivotal.pal.prerequisites

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import freemarker.template.Configuration
import khttp.get
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.handler.DefaultHandler
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.server.handler.ResourceHandler
import java.io.File
import java.lang.System.exit
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

fun main(args: Array<String>) {
    val env = Env()
    val server = Server(env.serverPort)
    ScorecardApplication(server, env)

    server.start()
    server.join()
}

class ScorecardApplication(
        server: Server,
        val env: Env
) : AbstractHandler() {

    init {
        val resourceHandler = ResourceHandler().apply {
            resourceBase = "src/main/resources/static"
        }

        val handlerList = HandlerList(this, resourceHandler, DefaultHandler())
        server.handler = handlerList

    }

    val freemarkerConfig = Configuration(Configuration.VERSION_2_3_23)
            .apply { setDirectoryForTemplateLoading(File("templates")) }

    val objectMapper = ObjectMapper().registerKotlinModule()

    override fun handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse) {
        when (request.requestURI) {
            "/" -> {
                val template = freemarkerConfig.getTemplate("welcome.ftl")
                template.process(null, response.writer)
            }

            "/scorecard" -> {
                val caddyUri = "${env.caddyUrl}/scorecard?email=${env.email}"
                val rawScorecardJson = get(caddyUri, headers = mapOf("X-CADDY-API-KEY" to env.apiKey)).text
                val scorecard = objectMapper.readValue(rawScorecardJson, ScorecardInfo::class.java)

                val template = freemarkerConfig.getTemplate("scorecard.ftl")
                template.process(scorecard, response.writer)
            }

            else -> {
                return
            }
        }

        baseRequest.isHandled = true
    }
}


data class AssignmentInfo(val name: String, val identifier: String, val success: Boolean?, val attempts: Int?)

data class UnitInfo(val name: String, val assignments: List<AssignmentInfo>)

data class ScorecardInfo(
        val studentName: String,
        val units: List<UnitInfo>
)

data class Env(
        val serverPort: Int = getEnvVar("PORT").toInt(),
        val caddyUrl: String = getEnvVar("CADDY_URL"),
        val email: String = getEnvVar("EMAIL"),
        val apiKey: String = getEnvVar("CADDY_API_KEY")
)

fun getEnvVar(name: String): String {
    try {
        return System.getenv(name)
    } catch (e: Exception) {
        println("$name was not set")
        exit(1)
        throw e
    }
}
