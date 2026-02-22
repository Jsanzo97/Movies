package jsanzo.movies.domain.model

data class DomainMovieProductionCompany(
    val name: String,
    val id: Int,
    val logoPath: String?,
    val originCountry: String,
)
