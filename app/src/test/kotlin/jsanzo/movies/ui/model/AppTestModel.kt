package jsanzo.movies.ui.model

import jsanzo.movies.domain.model.DomainMovie
import jsanzo.movies.domain.model.DomainMovieDetails
import jsanzo.movies.domain.model.DomainMovieResult

internal val domainMovieDetails = DomainMovieDetails(
    adult = false,
    backdropPath = null,
    belongsToCollection = null,
    budget = 0,
    genres = listOf(),
    homepage = null,
    id = 1,
    imdbId = null,
    originCountry = listOf(""),
    originalLanguage = "",
    originalTitle = "",
    overview = null,
    popularity = 0.0,
    posterPath = null,
    productionCompanies = listOf(),
    productionCountries = listOf(),
    releaseDate = "",
    revenue = 0,
    runtime = null,
    spokenLanguages = listOf(),
    status = "",
    tagline = null,
    title = "",
    video = false,
    voteAverage = 0.0,
    voteCount = 0,
)

internal val domainMovieResult = DomainMovieResult(
    posterPath = null,
    adult = false,
    overview = "",
    releaseDate = "",
    genreIds = listOf(),
    id = 1,
    originalTitle = "",
    originalLanguage = "",
    title = "",
    backdropPath = null,
    popularity = 0.0,
    voteCount = 0,
    video = false,
    voteAverage = 0.0,
)

internal val domainMovie = DomainMovie(
    page = 0,
    results = (1..20).map { id ->
        domainMovieResult.copy(id = id)
    },
    totalResults = 0,
    totalPages = 0,
)
