package jsanzo.movies.data.model

import jsanzo.movies.domain.model.DomainMovie

data class DataMovie(
    val page: Int,
    val results: List<DataMovieResult>,
    val totalResults: Int,
    val totalPages: Int,
)

fun DataMovie.toMovie() = DomainMovie(
    page,
    results.map { it.toMovieResult() },
    totalResults,
    totalPages,
)
