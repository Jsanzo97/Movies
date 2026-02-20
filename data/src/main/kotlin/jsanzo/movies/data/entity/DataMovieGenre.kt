package jsanzo.movies.data.entity

import jsanzo.movies.domain.entity.MovieGenre

data class DataMovieGenre(
    val id: Int,
    val name: String
)

fun DataMovieGenre.toMovieGenre() = MovieGenre(
    id, name
)
