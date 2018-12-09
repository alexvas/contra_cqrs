package contra.rest

import contra.dal.allMovies
import contra.dal.findMovie
import contra.dal.findMovieInInterval
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import java.time.Instant


val allMovies: Handler = { call.respond(allMovies()) }

val findMovie: Handler = {
    call.parameters["id"]
            ?.toInt()
            ?.let { id -> findMovie(id) }
            ?.let { movie -> call.respond(movie) }
            ?: call.respond(HttpStatusCode.NotFound, "not found")
}

val findMovieInInterval: Handler = {
    val startingIncluding: Instant?
    val endingExcluding: Instant?
    call.parameters.let { params ->
        startingIncluding = params["startingIncluding"]?.let { t -> instant(t) }
        endingExcluding = params["endingExcluding"]?.let { t -> instant(t) }
    }
    if (startingIncluding == null || endingExcluding == null) {
        call.respond(HttpStatusCode.NotFound, "not found")
    } else {
        call.respond(findMovieInInterval(startingIncluding, endingExcluding))
    }
}