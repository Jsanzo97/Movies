package jsanzo.movies.domain.entity

data class Movie(
    val page: Int,
    val results: List<MovieResult>,
    val totalResults: Int,
    val totalPages: Int
)

