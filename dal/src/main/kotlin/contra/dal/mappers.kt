package contra.dal

import contra.common.Cinema
import contra.common.Movie
import contra.common.Show
import org.apache.ibatis.annotations.Arg
import org.apache.ibatis.annotations.ConstructorArgs
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import java.time.Instant


interface CinemaMapper {

    @Select("SELECT * FROM cinema")
    fun all(): List<Cinema>

    @Select("SELECT * FROM cinema WHERE id = #{id}")
    fun find(@Param("id") id: Int): Cinema

}

interface MovieMapper {

    @Select("SELECT * FROM movie")
    fun all(): List<Movie>

    @Select("SELECT * FROM movie WHERE id = #{id}")
    fun find(@Param("id") id: Int): Movie

    /**
     * отдаём фильмы, которые показывают в заданный интервал времени, списком,
     * отсортированном по названию фильма
     */
    @Select("SELECT m.id, m.title\n" +
            "FROM movie m\n" +
            "       JOIN shows s ON m.id = s.movie_id\n" +
            "  AND s.start >= #{starting}\n" +
            "  AND #{ending} > s.start\n" +
            "GROUP BY m.id, title\n" +
            "ORDER BY m.title")
    fun findInInterval(
            @Param("starting") startingIncluding: Instant,
            @Param("ending") endingExcluding: Instant
    ): List<Movie>
}

data class ShowWithHallData(
        val hallNum: Int,
        val show: Show
) {
    @Suppress("unused")
    constructor(
            hallNum: Int,
            hallSeats: Int,
            id: Int,
            start: Instant,
            hallId: Int,
            movieId: Int,
            availableSeats: List<Int>
    ) : this(
            hallNum,
            Show(
                    id,
                    hallId,
                    movieId,
                    start,
                    availableSeats.asSequence()
                            .filter { it <= hallSeats }
                            .toSortedSet()
            )
    )
}

interface ShowMapper {
    /**
     * Отдаём сеансы вместе с кинозалом для заданных кинотеатра, фильма и интервала времени.
     * Данные отсортированы по номеру кинозала, а потом -- по времени начала фильма.
     */
    @ConstructorArgs(
            Arg(column = "num", javaType = Int::class),
            Arg(column = "seats_count", javaType = Int::class),
            Arg(column = "id", id = true, javaType = Int::class),
            Arg(column = "start", javaType = Instant::class),
            Arg(column = "hall_id", javaType = Int::class),
            Arg(column = "movie_id", javaType = Int::class),
            Arg(column = "seats", javaType = List::class, typeHandler = AvailableSeatsHandler::class)
    )
    @Select("SELECT h.num         AS num,\n" +
            "       h.seats_count AS seats_count,\n" +
            "       s.id          AS id,\n" +
            "       s.start       AS start,\n" +
            "       s.hall_id     AS hall_id,\n" +
            "       s.movie_id    AS movie_id,\n" +
            "       s.seats       AS seats\n" +
            "FROM shows s\n" +
            "       JOIN hall h on s.hall_id = h.id\n" +
            "WHERE s.start >= #{starting}\n" +
            "  AND #{ending} > s.start\n" +
            "  AND s.movie_id = #{movie_id}\n" +
            "  AND h.cinema_id = #{cinema_id}\n" +
            "ORDER BY h.num, s.start")
    fun findInInterval(
            @Param("movie_id") movieId: Int,
            @Param("cinema_id") cinemaId: Int,
            @Param("starting") startingIncluding: Instant,
            @Param("ending") endingExcluding: Instant
    ): List<ShowWithHallData>
}
