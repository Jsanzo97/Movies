package jsanzo.movies.data.entity

import jsanzo.movies.domain.entity.MovieCollection

data class DataMovieCollection(
    val id: Int,
    val name: String,
    val posterPath: String,
    val backdropPath: String,
)

fun DataMovieCollection.toMovieCollection() = MovieCollection(
    id,
    name,
    posterPath,
    backdropPath,
)
