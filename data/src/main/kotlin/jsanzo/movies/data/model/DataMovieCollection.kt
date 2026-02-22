package jsanzo.movies.data.model

import jsanzo.movies.domain.model.DomainMovieCollection

data class DataMovieCollection(
    val id: Int,
    val name: String,
    val posterPath: String,
    val backdropPath: String,
)

fun DataMovieCollection.toMovieCollection() = DomainMovieCollection(
    id,
    name,
    posterPath,
    backdropPath,
)
