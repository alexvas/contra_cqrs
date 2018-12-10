package contra.rest

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.*
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.method
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.pipeline.PipelineContext
import org.apache.logging.log4j.LogManager
import org.slf4j.event.Level
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

typealias Handler = suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit

const val dateFormatPattern = "yyyy-MM-dd'T'HH:mm"

val dtf = DateTimeFormatter.ofPattern(dateFormatPattern)
        .withZone(ZoneId.of("UTC"))!!

internal fun instant(input: String): Instant =
        LocalDateTime
                .parse(input, dtf)
                .toInstant(ZoneOffset.UTC)


private val log = LogManager.getLogger("application")!!


fun Application.module() {
    install(CallLogging) {
        level = Level.DEBUG
    }
    install(StatusPages) {
        exception<NumberFormatException> { cause ->
            log.debug("bad user input", cause)
            call.respond(HttpStatusCode.NotAcceptable, "not a number")
        }
        exception<DateTimeParseException> { cause ->
            log.debug("bad user input", cause)
            call.respond(HttpStatusCode.NotAcceptable, "not a date/time format. Valid format is ${dtf.format(Instant.now())}")
        }
        exception<IllegalArgumentException> { cause ->
            log.debug("bad user input", cause)
            call.respond(HttpStatusCode.NotAcceptable, "illegal argument")
        }
        exception<Throwable> { cause ->
            log.warn("internal", cause)
            call.respond(HttpStatusCode.InternalServerError, "internal")
        }
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            registerModule(JavaTimeModule())
            dateFormat = SimpleDateFormat(dateFormatPattern)
            disableDefaultTyping()
        }
    }
    routing {
        trace { application.log.info(it.buildText()) }
        method(HttpMethod.Get) {

            route("cinema") {
                route("all") { handle(allCinemas) }
                route("{id}") { handle(findCinema) }
            }

            route("movie") {
                route("all") { handle(allMovies) }
                route("{id}") { handle(findMovie) }
                route("{startingIncluding}/{endingExcluding}") { handle(findMovieInInterval) }
            }

            route("show") {
                route("{id}") { handle(findShow) }
                route("{movieId}/{cinemaId}/{startingIncluding}/{endingExcluding}") { handle(findShowInInterval) }
            }
        }

        method(HttpMethod.Post) {
            route("show/{id}/{seats}") { handle(book) }

        }


    }
}
