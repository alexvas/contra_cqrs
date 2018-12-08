package contra.dal

import com.ninja_squad.dbsetup.Operations
import com.ninja_squad.dbsetup_kotlin.insertInto
import contra.common.Cinema
import contra.common.Movie
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private val deleteAll = Operations.deleteAllFrom(
        "shows",
        "movie",
        "hall",
        "cinema"
)!!

private val cinema = insertInto("cinema") {
    columns("id", "name")
    values(1, "Гигант")
    values(2, "Пионер")
    values(3, "Аврора")
}

private val movie = insertInto("movie") {
    columns("id", "title")
    values(1, "Акула-каракула страйкс бэк")
    values(2, "Палки VIII")
    values(3, "Сумраки")
}

@Suppress("NonAsciiCharacters", "ClassName")
class DbReadTest {
    @BeforeAll
    fun setUp() {
        // все операции -- только чтение. Инициализировать БД надо один раз
        NinjaAdapter()
                .prepare(deleteAll, cinema, movie)
    }

    @Nested
    inner class Кинотеатры {

        @Test
        fun `а какие-такие вообще бывают кинотеатры?`() {
            assertThat(allCinemas()).isEqualTo(
                    listOf(
                            Cinema(1, "Гигант"),
                            Cinema(2, "Пионер"),
                            Cinema(3, "Аврора")
                    )
            )
        }

        @Test
        fun `найдём-ка второй кинотеатр в городе`() {
            assertThat(findCinema(2)).isEqualTo(
                    Cinema(2, "Пионер")
            )
        }
    }

    @Nested
    inner class Фильмы {

        @Test
        fun `а какие-такие вообще бывают фильмы?`() {
            assertThat(allMovies()).isEqualTo(
                    listOf(
                            Movie(1, "Акула-каракула страйкс бэк"),
                            Movie(2, "Палки VIII"),
                            Movie(3, "Сумраки")
                    )
            )
        }

        @Test
        fun `найдём-ка второй фильм в прокате`() {
            assertThat(findMovie(2)).isEqualTo(
                    Movie(2, "Палки VIII")
            )
        }
    }


}