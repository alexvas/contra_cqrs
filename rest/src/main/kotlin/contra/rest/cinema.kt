package contra.rest

import contra.dal.allCinemas
import contra.dal.findCinema
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond


val allCinemas: Handler = { call.respond(allCinemas()) }

val findCinema: Handler = {
    call.parameters["id"]
            ?.toInt()
            ?.let { id -> findCinema(id) }
            ?.let { cinema -> call.respond(cinema) }
            ?: call.respond(HttpStatusCode.NotFound, "not found")
}
