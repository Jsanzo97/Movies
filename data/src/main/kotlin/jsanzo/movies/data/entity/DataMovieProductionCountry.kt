package jsanzo.movies.data.entity

import jsanzo.movies.domain.entity.MovieProductionCountry

data class DataMovieProductionCountry(
    val iso: String,
    val name: String,
)

fun DataMovieProductionCountry.toMovieProductionCountry() = MovieProductionCountry(
    iso,
    name,
)
