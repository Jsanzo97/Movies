package service.model

import jsanzo.movies.remote.dto.response.GetMoviesDetailCollection
import jsanzo.movies.remote.dto.response.GetMoviesDetailsResponse
import jsanzo.movies.remote.dto.response.GetMoviesGenre
import jsanzo.movies.remote.dto.response.GetMoviesProductionCompany
import jsanzo.movies.remote.dto.response.GetMoviesProductionCountry
import jsanzo.movies.remote.dto.response.GetMoviesResponse
import jsanzo.movies.remote.dto.response.GetMoviesSpokenLanguage
import jsanzo.movies.remote.dto.response.MoviesResponseResult

internal val getMovieResponse = GetMoviesResponse(
    page = 1,
    results = listOf(
        MoviesResponseResult(
            posterPath = "",
            adult = false,
            overview = "",
            releaseDate = "",
            genreIds = listOf(1),
            id = 1,
            originalTitle = "",
            originalLanguage = "",
            title = "",
            backdropPath = "",
            popularity = 1.0,
            voteCount = 1,
            video = true,
            voteAverage = 1.0,
        ),
    ),
    totalPages = 1,
    totalResults = 0,
)

internal val getMovieDetails = GetMoviesDetailsResponse(
    id = 1,
    title = "Movie Title",
    overview = "Overview",
    releaseDate = "2024-01-01",
    posterPath = "/path.jpg",
    backdropPath = "/path.jpg",
    voteAverage = 8.0,
    genres = listOf(
        GetMoviesGenre(
            id = 1,
            name = "",
        ),
    ),
    productionCompanies = listOf(
        GetMoviesProductionCompany(
            id = 1,
            logoPath = "/path.jpg",
            name = "",
            originCountry = "",
        ),
    ),
    productionCountries = listOf(
        GetMoviesProductionCountry(
            iso = "",
            name = "",
        ),
    ),
    spokenLanguages = listOf(
        GetMoviesSpokenLanguage(
            englishName = "",
            iso = "",
            name = "",
        ),
    ),
    budget = 1_000_000,
    revenue = 5_000_000,
    runtime = 120,
    status = "Released",
    tagline = "Tagline",
    homepage = "homepage.com",
    adult = false,
    imdbId = "",
    originalLanguage = "en",
    originalTitle = "Movie Title",
    popularity = 10.0,
    video = false,
    voteCount = 100,
    belongsToCollection = GetMoviesDetailCollection(
        id = 1,
        name = "",
        posterPath = "",
        backdropPath = "",
    ),
    originCountry = listOf("esp"),
)
