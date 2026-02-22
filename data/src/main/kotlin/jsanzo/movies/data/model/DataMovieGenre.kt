package jsanzo.movies.data.model

import jsanzo.movies.domain.model.DomainMovieGenre

data class DataMovieGenre(
    val id: Int,
    val name: String,
)

fun DataMovieGenre.toMovieGenre() = DomainMovieGenre(
    id,
    name,
)
