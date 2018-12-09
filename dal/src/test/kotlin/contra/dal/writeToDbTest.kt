package contra.dal

import com.ninja_squad.dbsetup.Operations
import com.ninja_squad.dbsetup_kotlin.insertInto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private val deleteAllFromShows = Operations.deleteAllFrom("shows")

private val shows = insertInto("shows") {
    columns("id", "start", "hall_id", "movie_id", "seats")
    values(1, starts[0], 5, 3, generateBookedSeats())
}

private fun generateBookedSeats(): String {
    return buildSeatsBitString(listOf(5, 7, 10))
}

@Suppress("NonAsciiCharacters", "ClassName")
class DbWriteTest {
    private lateinit var ninja: NinjaAdapter

    @BeforeAll
    fun setUp() {
        configurePools("jdbc:postgresql://localhost:5432/contra_cqrs", "contra_cqrs")
        configureSessionFactory()
        ninja = NinjaAdapter(writeOnlyDataSource)
                .prepare(deleteAll, cinema, movie, hall)
    }

    @AfterAll
    fun tearDown() {
        closePools()
    }

    @BeforeEach
    fun setUpEach() {
        ninja.prepare(deleteAllFromShows, shows)
    }

    @Test
    fun `сеанс в БД такой, каким мы его сконфигурировали`() {
        val show = findShow(1)
        assertThat(show.availableSeats.size).isEqualTo(97)
        assertThat(show.availableSeats.max()).isEqualTo(100)
        assertThat(show.availableSeats.min()).isEqualTo(1)
        assertThat(show.availableSeats).doesNotContain(5, 7, 10)
        ninja.skipNextLaunch()
    }

    @Test
    fun `бронируем место в начале`() {
        assertThat(book(findShow(1), setOf(1))).isTrue()
        val updated = findShow(1)
        assertThat(updated.availableSeats.size).isEqualTo(96)
        assertThat(updated.availableSeats.max()).isEqualTo(100)
        assertThat(updated.availableSeats.min()).isEqualTo(2)
        assertThat(updated.availableSeats).doesNotContain(1, 5, 7, 10)
    }

    @Test
    fun `бронируем место в конце`() {
        assertThat(book(findShow(1), setOf(100))).isTrue()
        val updated = findShow(1)
        assertThat(updated.availableSeats.size).isEqualTo(96)
        assertThat(updated.availableSeats.max()).isEqualTo(99)
        assertThat(updated.availableSeats.min()).isEqualTo(1)
        assertThat(updated.availableSeats).doesNotContain(5, 7, 10, 100)
    }

    @Test
    fun `бронируем кучу мест`() {
        assertThat(book(findShow(1), setOf(11, 12, 13, 14, 15, 16, 17, 18, 19, 20))).isTrue()
        val updated = findShow(1)
        assertThat(updated.availableSeats.size).isEqualTo(87)
        assertThat(updated.availableSeats.max()).isEqualTo(100)
        assertThat(updated.availableSeats.min()).isEqualTo(1)
        assertThat(updated.availableSeats).doesNotContain(5, 7, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
    }

    @Test
    fun `не удалось забронировать уже забронированное`() {
        assertThat(book(findShow(1), setOf(7, 8, 9))).isFalse()
        val unmodified = findShow(1)
        assertThat(unmodified.availableSeats.size).isEqualTo(97)
        assertThat(unmodified.availableSeats.max()).isEqualTo(100)
        assertThat(unmodified.availableSeats.min()).isEqualTo(1)
        assertThat(unmodified.availableSeats).doesNotContain(5, 7, 10)
        ninja.skipNextLaunch()
    }


}