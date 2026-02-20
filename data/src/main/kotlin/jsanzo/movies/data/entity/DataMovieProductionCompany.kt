package jsanzo.movies.data.entity

import jsanzo.movies.domain.entity.MovieProductionCompany

data class DataMovieProductionCompany(
    val name: String,
    val id: Int,
    val logoPath: String?,
    val originCountry: String
)

fun DataMovieProductionCompany.toMovieProductionCompany() = MovieProductionCompany(
    name, id, logoPath, originCountry
)

