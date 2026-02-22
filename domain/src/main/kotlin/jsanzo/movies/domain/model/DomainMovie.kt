package jsanzo.movies.domain.model

data class DomainMovie(
    val page: Int,
    val results: List<DomainMovieResult>,
    val totalResults: Int,
    val totalPages: Int,
)
