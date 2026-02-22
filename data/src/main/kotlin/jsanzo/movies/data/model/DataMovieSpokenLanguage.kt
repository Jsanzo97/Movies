package jsanzo.movies.data.model

import jsanzo.movies.domain.model.DomainMovieSpokenLanguage

data class DataMovieSpokenLanguage(
    val iso: String,
    val name: String,
)

fun DataMovieSpokenLanguage.toMovieSpokenLanguage() = DomainMovieSpokenLanguage(
    iso,
    name,
)
