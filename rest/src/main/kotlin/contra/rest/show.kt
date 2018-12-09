package contra.rest

import contra.common.Show
import contra.dal.book
import contra.dal.findShow
import contra.dal.findShowInInterval
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import java.time.Instant

val findShowInInterval: Handler = {
    val movieId: Int?
    val cinemaId: Int?
    val startingIncluding: Instant?
    val endingExcluding: Instant?
    call.parameters.let { params ->
        movieId = params["movieId"]?.toInt()
        cinemaId = params["cinemaId"]?.toInt()
        startingIncluding = params["startingIncluding"]?.let { t -> instant(t) }
        endingExcluding = params["endingExcluding"]?.let { t -> instant(t) }
    }
    if (
            movieId == null ||
            cinemaId == null ||
            startingIncluding == null ||
            endingExcluding == null
    ) {
        call.respond(HttpStatusCode.NotFound, "not found")
    } else {
        call.respond(findShowInInterval(movieId, cinemaId, startingIncluding, endingExcluding))
    }
}

val book: Handler = {
    val show: Show?
    val seats: Set<Int>?
    call.parameters.let { params ->
        show = params["showId"]?.toInt()?.let { id -> findShow(id) }
        seats = params["seats"]?.splitToSequence(',')
                ?.map { chunk -> chunk.toInt() }
                ?.toSet()
    }
    if (
            show == null ||
            seats == null ||
            seats.isEmpty()
    ) {
        call.respond(HttpStatusCode.NotFound, "not found")
    } else {
        call.respond(book(show, seats))
    }
}