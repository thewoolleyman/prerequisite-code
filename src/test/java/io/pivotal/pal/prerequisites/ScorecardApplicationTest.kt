import khttp.get
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.io.IOException
import java.lang.Thread.sleep
import java.net.Socket

class ScorecardApplicationTests {

    @Test
    fun itShowsTheWelcomePage() {
        assertThat(get(serverUrl).text).contains("Welcome to PAL")
    }

    @Test
    fun itRendersTheScorecard() {
        val body = get("$serverUrl/scorecard").text
        val doc = Jsoup.parse(body)

        assertThat(doc.select("section h1").text()).isEqualTo("Test Student")
        val header = listOf(
                "Prerequisites",
                "Prerequisites",
                "Ponies",
                "Making a pony",
                "Riding ponies"
        )

        assertThat(doc.select("table tr:nth-of-type(1) th").map { it.text() }).isEqualTo(header)
    }

    @Test
    fun itRendersAttempts() {
        val body = get("$serverUrl/scorecard").text
        val doc = Jsoup.parse(body)

        val attempts = listOf(
                "",
                "1",
                "",
                "2",
                "-"
        )

        assertThat(doc.select("table td").map { it.text() }).isEqualTo(attempts)
        assertThat(doc.select("table td:nth-of-type(2)").attr("class")).contains("success")
        assertThat(doc.select("table td:nth-of-type(4)").attr("class")).contains("failure")
        assertThat(doc.select("table td:nth-of-type(5)").attr("class")).contains("unknown")
    }

    companion object {
        var server: Process? = null
        lateinit var env: Map<String, String>
        lateinit var serverUrl: String
        lateinit var caddyServer: MockWebServer

        //language=JSON
        val JSON_RESPONSE = """
{
  "studentName": "Test Student",
  "units": [
    {
      "name": "Prerequisites",
      "assignments": [
        {
          "name": "Prerequisites",
          "identifier": "prerequisites",
          "success": true,
          "attempts": 1
        }
      ]
    },
    {
      "name": "Ponies",
      "assignments": [
        {
          "name": "Making a pony",
          "identifier": "making-ponies",
          "success": false,
          "attempts": 2
        },
        {
          "name": "Riding ponies",
          "identifier": "pony-riding",
          "success": false,
          "attempts": 0
        }
      ]
    }
  ]
}
"""
        @JvmStatic
        @BeforeClass
        fun setUp() {
            val caddyServer = MockWebServer()

            caddyServer.enqueue(MockResponse().setBody(JSON_RESPONSE))
            caddyServer.enqueue(MockResponse().setBody(JSON_RESPONSE))

            caddyServer.start()

            env = mapOf(
                    "PORT" to "8085",
                    "CADDY_URL" to caddyServer.url("/").toString(),
                    "EMAIL" to "test@example.com",
                    "CADDY_API_KEY" to "api-key"
            )

            serverUrl = "http://localhost:${env["PORT"]}"

            server = startServer(env)
        }

        @JvmStatic
        @AfterClass
        fun tearDown() {
            stopServer()
        }


        private fun stopServer() {
            server!!.destroy()
        }

        private fun startServer(env: Map<String, String>): Process {
            val builder = ProcessBuilder("java", "-jar", "./build/libs/prerequisite.jar")
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
            builder.environment().putAll(env)

            val process = builder.start()
            waitForPort(env["PORT"]!!.toInt())
            return process
        }

        private fun waitForPort(port: Int) {
            var tries = 0

            while (tries < 50) {
                try {
                    Socket("localhost", port)
                    return
                } catch(e: IOException) {

                }

                tries += 1
                sleep(100)
            }

            throw Exception("server failed to start")
        }
    }
}
