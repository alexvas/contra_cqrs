package contra.res

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import contra.common.Cinema
import contra.common.Conf
import contra.common.Movie
import contra.common.Show
import contra.dal.*
import contra.rest.dateFormatPattern
import contra.rest.startServer
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
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
import java.text.SimpleDateFormat
import java.util.concurrent.ThreadFactory

var clientThreadNum = 0
val clientThreadFactory = ThreadFactory { Thread(it, "Ktor-client-${++clientThreadNum}").apply { isDaemon = true } }

internal val httpClient = HttpClient(Apache) {
    install(JsonFeature) {
        serializer = JacksonSerializer {
            registerModule(JavaTimeModule())
            dateFormat = SimpleDateFormat(dateFormatPattern)
        }
    }

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

private fun builder() = HttpRequestBuilder().apply {
    url.host = conf.host()
    url.port = conf.port()
    method = HttpMethod.Get // default value
}

@Suppress("NonAsciiCharacters")
class IntegrationTests : CoroutineScope {

    override val coroutineContext = Job()

    private inline fun <reified T> call(crossinline requestConfig: (HttpRequestBuilder) -> Unit): T = runBlocking(coroutineContext) {
        val builder = builder()
        requestConfig.invoke(builder)
        httpClient.request<T>(builder)
    }

    private inline fun <reified T> get(vararg components: String) = call<T> {
        it.url.path(*components)
    }

    private inline fun <reified T> post(vararg components: String) = call<T> {
        it.url.path(*components)
        it.method = HttpMethod.Post
    }

    @BeforeAll
    fun beforeAll() {
        startServer(conf, false)
        initDbWithTestData()
    }

    @Test
    fun `а что нынче показывают?`() {
        // curl -H "Content-Type: application/json" 127.0.0.1:8080/movie/all
        val response = get<List<Movie>>("movie", "all")
        assertThat(response).isEqualTo(ethaloneMovies)
    }

    @Test
    fun `а в каких кинотеатрах?`() {
        // curl -H "Content-Type: application/json" 127.0.0.1:8080/cinema/all
        val response = get<List<Cinema>>("cinema", "all")
        assertThat(response).isEqualTo(ethaloneCinemas)
    }

    @Test
    fun `пойдём в "Гигант"!`() {
        // curl -H "Content-Type: application/json" 127.0.0.1:8080/cinema/1
        val response = get<Cinema>("cinema", "1")
        assertThat(response).isEqualTo(ethaloneCinemas[0])
    }

    @Test
    fun `на "Акулу-каракулу"!`() {
        // curl -H "Content-Type: application/json" 127.0.0.1:8080/movie/1
        val response = get<Movie>("movie", "1")
        assertThat(response).isEqualTo(ethaloneMovies[0])
    }

    @Test
    fun `кино #99 у нас не показывают, мальчик`() {
        // curl -H "Content-Type: application/json" 127.0.0.1:8080/movie/99
        val response = runBlocking(coroutineContext) {
            httpClient.call(builder().apply {
                url.path("movie", "99")
            }).response
        }
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(runBlocking { response.readText() }).isEqualTo("not found")
    }

    private fun book(showId: Int, seats: Iterable<Int>): Boolean =
            post("show", showId.toString(), seats.joinToString(","))

    private fun findShow(showId: Int): Show = get("show", showId.toString())

    @Test
    fun `ищем ближайший сеанс "Акулы-каракулы" в "Гиганте" и занимаем там места`() {
        // curl -H "Content-Type: application/json" 127.0.0.1:8080/show/1/1/2018-01-01T00:00/2030-12-31T23:59
        val response = get<Map<Int, List<Show>>>("show", "1", "1", "2018-01-01T00:00", "2030-12-31T23:59")
        assertThat(response).isNotEmpty
        val earliestShow = response.asSequence()
                .flatMap { it.value.asSequence() }
                .filter { it.availableSeats.isNotEmpty() }
                .filter { it.availableSeats.size > 10 }
                .minBy { it.start }!!
        val availableBeforeBooking = earliestShow.availableSeats
        val toBook = availableBeforeBooking.toList().subList(0, 5) // выбрали 5 свободных мест


        // curl -X POST -H "Content-Type: application/json" 127.0.0.1:8080/show/<id сеанса>/<места через запятую>
        val bookingResponse = book(earliestShow.id, toBook)
        assertThat(bookingResponse).isTrue()

        // curl -H "Content-Type: application/json" 127.0.0.1:8080/show/<id сеанса>
        val showAfterBooking = findShow(earliestShow.id)

        val availableAfterBooking = showAfterBooking.availableSeats
        assertThat(availableBeforeBooking.size - availableAfterBooking.size).isEqualTo(5) // число свободных мест уменьшилось на 5

        // схитрим: попробуем взять только что занятое место и ещё одно свободное и снова занять их
        val toDoubleBook = listOf(toBook[0], availableAfterBooking.first())
        val doubleBookingResponse = book(earliestShow.id, toDoubleBook)
        assertThat(doubleBookingResponse).isFalse()
        val showAfterDoubleBooking = findShow(earliestShow.id)
        assertThat(showAfterDoubleBooking)
                .describedAs("после неудачного занятия мест ничего и не поменялось")
                .isEqualTo(showAfterBooking)
    }


    @AfterAll
    fun afterAll() {
        closePools()
    }
}