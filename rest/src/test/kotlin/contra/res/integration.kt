package contra.res

import contra.common.Cinema
import contra.common.Conf
import contra.common.Movie
import contra.dal.closePools
import contra.dal.ethaloneCinemas
import contra.dal.ethaloneMovies
import contra.dal.initDbWithTestData
import contra.rest.start
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.response.readText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.aeonbits.owner.ConfigFactory
import org.apache.http.impl.nio.reactor.IOReactorConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.concurrent.ThreadFactory

var clientThreadNum = 0
val clientThreadFactory = ThreadFactory { Thread(it, "Ktor-client-${++clientThreadNum}").apply { isDaemon = true } }

internal val httpClient = HttpClient(Apache) {
    install(JsonFeature)

    expectSuccess = false
    engine {
        followRedirects = false
        connectTimeout = 1_000
        socketTimeout = 1_000
        connectionRequestTimeout = 2_000
        customizeClient {
            setDefaultIOReactorConfig(
                    IOReactorConfig.custom().apply {
                        setMaxConnPerRoute(1)
                        setMaxConnTotal(1)
                        setIoThreadCount(1)
                    }.build()
            )
            setThreadFactory(clientThreadFactory)
        }
    }
}

val conf = ConfigFactory.create(Conf::class.java)!!

internal suspend inline fun <reified T> call(requestConfig: (HttpRequestBuilder) -> Unit): T {
    val builder = builder()
    requestConfig.invoke(builder)
    return httpClient.request(builder)
}

private fun builder() =
        HttpRequestBuilder().apply {
            url.host = conf.host()
            url.port = conf.port()
            method = HttpMethod.Get // default value
        }

@Suppress("NonAsciiCharacters")
class IntegrationTests : CoroutineScope {

    override val coroutineContext = Job()

    @BeforeAll
    fun beforeAll() {
        start(conf, false)
        initDbWithTestData()
    }

    @Test
    fun `а что нынче показывают?`() {
        val response = runBlocking(coroutineContext) {
            call<List<Movie>> {
                // curl -H "Content-Type: application/json" 127.0.0.1:8080/movie/all
                it.url.path("movie", "all")
            }
        }
        assertThat(response).isEqualTo(ethaloneMovies)
    }

    @Test
    fun `а в каких кинотеатрах?`() {
        val response = runBlocking(coroutineContext) {
            call<List<Cinema>> {
                // curl -H "Content-Type: application/json" 127.0.0.1:8080/cinema/all
                it.url.path("cinema", "all")
            }
        }
        assertThat(response).isEqualTo(ethaloneCinemas)
    }

    @Test
    fun `пойдём в "Гигант"!`() {
        val response = runBlocking(coroutineContext) {
            call<Cinema> {
                // curl -H "Content-Type: application/json" 127.0.0.1:8080/cinema/1
                it.url.path("cinema", "1")
            }
        }
        assertThat(response).isEqualTo(ethaloneCinemas[0])
    }

    @Test
    fun `на "Акулу-каракулу"!`() {
        val response = runBlocking(coroutineContext) {
            call<Movie> {
                // curl -H "Content-Type: application/json" 127.0.0.1:8080/movie/1
                it.url.path("movie", "1")
            }
        }
        assertThat(response).isEqualTo(ethaloneMovies[0])
    }

    @Test
    fun `такое кино у нас не показывают, мальчик`() {
        // curl -H "Content-Type: application/json" 127.0.0.1:8080/movie/99
        val response = runBlocking(coroutineContext) {
            httpClient.call(builder().apply {
                url.path("movie", "99")
            }).response
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(runBlocking { response.readText() }).isEqualTo("not found")
    }


    @AfterAll
    fun afterAll() {
        closePools()
    }
}