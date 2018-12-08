package contra.common

import java.time.Instant

/**
 * кинотеатр
 */
data class Cinema(
        val id: Int,
        val name: String
)

/**
 * фильма
 */
data class Movie(
        val id: Int,
        val title: String
)

/**
 * зал в кинотеатре
 */
data class Hall(
        val id: Int,
        val cinemaId: Int,
        /**
         * номер зала в кинотеатре
         */
        val num: Int,
        val seatsCount: Int
)

/**
 * сеанс
 */
data class Show(
        val id: Int,
        val hallId: Int,
        val movieId: Int,
        /**
         * время начала сеанса. Для простоты считаем, что длительность сеансов одинаковая
         */
        val start: Instant,
        val availableSeats: Set<Int>
)