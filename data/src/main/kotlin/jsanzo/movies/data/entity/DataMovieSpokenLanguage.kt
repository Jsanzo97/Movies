package jsanzo.movies.data.entity

import jsanzo.movies.domain.entity.MovieSpokenLanguage

data class DataMovieSpokenLanguage(
    val iso: String,
    val name: String
)

fun DataMovieSpokenLanguage.toMovieSpokenLanguage() = MovieSpokenLanguage(
    iso, name
)