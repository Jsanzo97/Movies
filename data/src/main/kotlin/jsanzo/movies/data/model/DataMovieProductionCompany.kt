package jsanzo.movies.data.model

import jsanzo.movies.domain.model.DomainMovieProductionCompany

data class DataMovieProductionCompany(
    val name: String,
    val id: Int,
    val logoPath: String?,
    val originCountry: String,
)

fun DataMovieProductionCompany.toMovieProductionCompany() = DomainMovieProductionCompany(
    name,
    id,
    logoPath,
    originCountry,
)
