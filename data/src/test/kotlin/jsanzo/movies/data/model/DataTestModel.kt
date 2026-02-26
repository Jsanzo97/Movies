package jsanzo.movies.data.model

import jsanzo.movies.domain.model.DomainMovieResult

internal val dataMovieResult = DataMovieResult(
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

internal val dataMovie = DataMovie(
    page = 1,
    results = listOf(dataMovieResult),
    totalResults = 1,
    totalPages = 1,
)

internal val dataMovieDetails = DataMovieDetails(
    adult = false,
    backdropPath = null,
    belongsToCollection = DataMovieCollection(
        id = 1,
        name = "Collection",
        posterPath = "",
        backdropPath = "",
    ),
    budget = 0,
    genres = listOf(DataMovieGenre(id = 1, name = "Action")),
    homepage = null,
    id = 1,
    imdbId = null,
    originCountry = listOf(""),
    originalLanguage = "",
    originalTitle = "",
    overview = null,
    popularity = 0.0,
    posterPath = null,
    productionCompanies = listOf(DataMovieProductionCompany(name = "Warner", id = 1, logoPath = null, originCountry = "US")),
    productionCountries = listOf(DataMovieProductionCountry(iso = "US", name = "United States")),
    releaseDate = "",
    revenue = 0,
    runtime = null,
    spokenLanguages = listOf(DataMovieSpokenLanguage(englishName = "name", iso = "en", name = "English")),
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
