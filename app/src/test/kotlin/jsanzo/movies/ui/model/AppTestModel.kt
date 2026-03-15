package jsanzo.movies.ui.model

import jsanzo.movies.domain.model.DomainMovie
import jsanzo.movies.domain.model.DomainMovieDetails
import jsanzo.movies.ui.screens.home.MovieUi
import kotlinx.collections.immutable.persistentListOf

internal val movieUi = MovieUi(
    id = 1,
    title = "",
    originalTitle = "",
    originalLanguage = "",
    overview = "",
    posterPath = "",
    backdropPath = "",
    releaseDate = "",
    popularity = 0.0,
    voteAverage = "0.0",
    voteCount = 0,
    adult = false,
    video = false,
    genreIds = persistentListOf(),
)

internal val domainMovieDetails = DomainMovieDetails(
    adult = false,
    backdropPath = "",
    belongsToCollection = null,
    budget = 0,
    genres = listOf(),
    homepage = "",
    id = 1,
    imdbId = "",
    originCountry = listOf(""),
    originalLanguage = "",
    originalTitle = "",
    overview = "",
    popularity = 0.0,
    posterPath = "",
    productionCompanies = listOf(),
    productionCountries = listOf(),
    releaseDate = "",
    revenue = 0,
    runtime = 0,
    spokenLanguages = listOf(),
    status = "",
    tagline = "",
    title = "",
    video = false,
    voteAverage = 0.0,
    voteCount = 0,
)

internal val domainMovie = DomainMovie(
    posterPath = "",
    adult = false,
    overview = "",
    releaseDate = "",
    genreIds = listOf(),
    id = 1,
    originalTitle = "",
    originalLanguage = "",
    title = "",
    backdropPath = "",
    popularity = 0.0,
    voteCount = 0,
    video = false,
    voteAverage = 0.0,
)

internal val listOfDomainMovie =
    (1..20).map { id ->
        domainMovie.copy(id = id)
    }.toList()

internal val listOfDomainMovie2 =
    (21..40).map { id ->
        domainMovie.copy(id = id)
    }.toList()
