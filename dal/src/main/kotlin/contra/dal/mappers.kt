package contra.dal

import contra.common.Cinema
import contra.common.Movie
import contra.common.Show
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

data class ShowWithHallNum(
        val hallNum: Int,
        val show: Show
) {
    @Suppress("unused")
    constructor(
            hallNum: Int,
            id: Int,
            start: Instant,
            hallId: Int,
            movieId: Int,
            availableSeats: Set<Int>
    ) : this(hallNum, Show(id, hallId, movieId, start, availableSeats))
}

interface ShowMapper {
    /**
     * Отдаём сеансы вместе с кинозалом для заданных кинотеатра, фильма и интервала времени.
     * Данные отсортированы по номеру кинозала, а потом -- по времени начала фильма.
     */
    @Select("SELECT h.num, s.*\n" +
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
    ): List<ShowWithHallNum>
}
