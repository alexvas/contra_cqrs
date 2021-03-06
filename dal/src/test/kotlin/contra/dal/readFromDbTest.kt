package contra.dal

import com.ninja_squad.dbsetup.Operations
import com.ninja_squad.dbsetup_kotlin.insertInto
import contra.common.Cinema
import contra.common.Conf
import contra.common.Movie
import contra.common.Show
import org.aeonbits.owner.ConfigFactory
import org.apache.logging.log4j.LogManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

internal val deleteAll = Operations.deleteAllFrom(
        "shows",
        "movie",
        "hall",
        "cinema"
)!!

val ethaloneCinemas = listOf(
        Cinema(1, "Гигант"),
        Cinema(2, "Пионер"),
        Cinema(3, "Аврора")
)

internal val cinema = insertInto("cinema") {
    columns("id", "name")
    ethaloneCinemas.forEach {
        values(it.id, it.name)
    }
}

val ethaloneMovies = listOf(
        Movie(1, "Акула-каракула страйкс бэк"),
        Movie(2, "Палки VIII"),
        Movie(3, "Сумраки"),
        Movie(4, "Белоснежко и семь женишков")
)

internal val movie = insertInto("movie") {
    columns("id", "title")
    ethaloneMovies.forEach {
        values(it.id, it.title)
    }
}

internal val hall = insertInto("hall") {
    var id = 0
    columns("id", "num", "cinema_id")

    repeat(10) { values(++id, it + 1, 1) } // 10 залов в "Гиганте"
    repeat(5) { values(++id, it + 1, 2) } // 5 залов в "Пионере"
    repeat(7) { values(++id, it + 1, 3) } // 7 залов в "Авроре"

    withDefaultValue("seats_count", 100) // потому что лень возиться с разными значениями
}

private val after1h = afterHours(1)
public val after2h = afterHours(2)
private val after3h = afterHours(3)
private val after4h = afterHours(4)
private val after5h = afterHours(5)
private val after6h = afterHours(6)
private val after7h = afterHours(7)
private val after8h = afterHours(8)

private fun afterHours(num: Int) = Instant.now().plus(num.toLong(), ChronoUnit.HOURS)!!

internal val starts = listOf(after1h, after2h, after3h, after4h, after5h, after6h, after7h, after8h)


private val shows = insertInto("shows") {
    var id = 0
    columns("id", "start", "hall_id", "movie_id", "seats")
    for (start in starts)
        for (hall in 1..22)
            values(++id, start, hall, Random.nextInt(1, 4), generateRandomBookedSeats())
}

private fun generateRandomBookedSeats(): String {
    val allSeats = mutableListOf<Int>()
    repeat(100) { allSeats += it + 1 }
    val bookedSeats = mutableSetOf<Int>()
    repeat(Random.nextInt(1, 100)) {
        bookedSeats += allSeats.removeAt(Random.nextInt(0, allSeats.lastIndex))
    }
    return buildSeatsBitString(bookedSeats)
}

fun buildSeatsBitString(bookedSeats: Collection<Int>): String {
    val buf = StringBuffer()
    repeat(100) {
        buf.append(if (bookedSeats.contains(it + 1)) 1 else 0)
    }
    return "$buf :: other"
}

fun initDbWithTestData() = NinjaAdapter()
            .prepare(deleteAll, cinema, movie, hall, shows)

@Suppress("NonAsciiCharacters", "ClassName")
class DbReadTest {
    private val log = LogManager.getLogger()

    @BeforeAll
    fun setUp() {
        val conf = ConfigFactory.create(Conf::class.java)!!
        configurePools(conf.jdbcUrl(), conf.dbUser(), conf.dbPassword().ifEmpty { null })

        // все операции -- только чтение. Инициализировать БД надо один раз
        initDbWithTestData()
    }

    @AfterAll
    fun tearDown() {
        closePools()
    }

    @Nested
    inner class Кинотеатры {

        @Test
        fun `а какие-такие вообще бывают кинотеатры?`() {
            assertThat(allCinemas()).isEqualTo(ethaloneCinemas)
        }

        @Test
        fun `найдём-ка второй кинотеатр в городе`() {
            assertThat(findCinema(2)).isEqualTo(ethaloneCinemas[1])
        }
    }

    @Nested
    inner class Фильмы {

        @Test
        fun `а какие-такие вообще бывают фильмы?`() {
            assertThat(allMovies()).isEqualTo(ethaloneMovies)
        }

        @Test
        fun `найдём-ка второй фильм в прокате`() {
            assertThat(findMovie(2)).isEqualTo(ethaloneMovies[1])
        }

        @Test
        fun `какие фильмы крутят через пару часов?`() {
            val movies = findMovieInInterval(after2h, after2h.plusSeconds(1))
            log.info("а через пару часов будут идти {}", movies)
            assertThat(movies).isNotEmpty
            assertThat(movies.size).isLessThan(5)
        }
    }

    @Nested
    inner class Сеансы {

        @Test
        fun `произвольный сеанс`() {
            val show = findShow(7)!!
            assertThat(show.availableSeats).isNotEmpty
            assertThat(show.availableSeats.size).isLessThan(100)
        }

        @Test
        fun `когда в «Гиганте» показывают «Акулу-каракулу»?`() {
            val shows = findShowInInterval(
                    1, 1, Instant.now(), afterHours(24)
            );
            log.info("а вот как показывают: \n{}", dump(shows))
            assertThat(shows).isNotEmpty
            assertThat(shows.size).isLessThan(11)
            assertThat(shows.values.asSequence().map { it.size }.max()).isGreaterThan(0)
        }
    }

    private fun dump(movies: Map<Int, List<Show>>): String = movies.asSequence().map { (key, list) ->
        key.toString() + "\n" + list.joinToString("\n")
    }.joinToString("\n")
}