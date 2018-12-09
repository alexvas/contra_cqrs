package contra.dal

import contra.common.Cinema
import contra.common.Movie
import contra.common.Show
import java.time.Instant

fun allCinemas() = mapper<CinemaMapper>().all()

fun findCinema(id: Int): Cinema {
    require(id > 0) {
        "non-positive id $id"
    }
    return mapper<CinemaMapper>().find(id)
}

fun allMovies() = mapper<MovieMapper>().all()

fun findMovie(id: Int): Movie {
    require(id > 0) {
        "non-positive id $id"
    }
    return mapper<MovieMapper>().find(id)
}

fun findMovieInInterval(startingIncluding: Instant, endingExcluding: Instant): List<Movie> {
    require(startingIncluding.isBefore(endingExcluding)) {
        "start $startingIncluding is not before end $endingExcluding"
    }

    return mapper<MovieMapper>().findInInterval(
            startingOrNow(startingIncluding, endingExcluding),
            endingExcluding
    )
}

fun findShowInInterval(movieId: Int, cinemaId: Int, startingIncluding: Instant, endingExcluding: Instant): Map<Int, List<Show>> {
    require(movieId > 0) {
        "non-positive movie id $movieId"
    }

    require(cinemaId > 0) {
        "non-positive cinema id $cinemaId"
    }


    require(startingIncluding.isBefore(endingExcluding)) {
        "start $startingIncluding is not before end $endingExcluding"
    }

    return mapper<ShowMapper>().findInInterval(
            movieId,
            cinemaId,
            startingOrNow(startingIncluding, endingExcluding),
            endingExcluding
    ).asSequence()
            .groupBy(ShowWithHallData::hallNum, ShowWithHallData::show)

}

/* todo: это не совсем корректно
     потому что кто-нибудь захочет узнать про фильмы в недавнем прошлом
     (которые идут прямо сейчас, например)
     однако здесь упрощаем ситуацию:
     всегда отрезаем старт интервала по настоящему моменту */
private fun startingOrNow(startingIncluding: Instant, endingExcluding: Instant): Instant {
    val mostRecent = maxOf(Instant.now()!!, startingIncluding)
    require(mostRecent.isBefore(endingExcluding)) {
        "$endingExcluding is in the past"
    }
    return mostRecent
}
