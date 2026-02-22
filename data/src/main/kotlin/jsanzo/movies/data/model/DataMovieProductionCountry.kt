package jsanzo.movies.data.model

import jsanzo.movies.domain.model.DomainMovieProductionCountry

data class DataMovieProductionCountry(
    val iso: String,
    val name: String,
)

fun DataMovieProductionCountry.toMovieProductionCountry() = DomainMovieProductionCountry(
    iso,
    name,
)
