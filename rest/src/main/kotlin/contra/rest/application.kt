package contra.rest

import com.fasterxml.jackson.databind.SerializationFeature
import contra.dal.allCinemas
import contra.dal.allMovies
import contra.dal.findCinema
import contra.dal.findMovie
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.method
import io.ktor.routing.route
import io.ktor.routing.routing
import org.slf4j.event.Level
import java.text.DateFormat

fun Application.module() {
    install(CallLogging) {
        level = Level.DEBUG
    }
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            dateFormat = DateFormat.getDateInstance()
            disableDefaultTyping()
        }
    }
    routing {
        method(HttpMethod.Get) {
            route("cinema") {
                route("all") {
                    handle { call.respond(allCinemas()) }
                }
                route("{id}") {
                    handle { call.respond(findCinema(call.parameters["id"]!!.toInt())) }
                }
            }
            route("movie") {
                route("all") {
                    handle { call.respond(allMovies()) }
                }
                route("{id}") {
                    handle { call.respond(findMovie(call.parameters["id"]!!.toInt())) }
                }
/*
                route("{startingIncluding}/{endingExcluding}") {
                    handle {
                        call.respond(
                                findMovieInInterval(
                                        call.parameters["startingIncluding"],
                                        call.parameters["endingExcluding"]
                                )
                        )
                    }
                }
*/
            }
        }


    }
}
