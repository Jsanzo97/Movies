package jsanzo.movies.data.model

import jsanzo.movies.domain.model.DomainMovieSpokenLanguage

data class DataMovieSpokenLanguage(
    val englishName: String,
    val iso: String,
    val name: String,
)

fun DataMovieSpokenLanguage.toMovieSpokenLanguage() = DomainMovieSpokenLanguage(
    englishName,
    iso,
    name,
)
