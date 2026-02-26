package jsanzo.movies.database.model

import jsanzo.movies.data.model.DataMovieCollection
import jsanzo.movies.data.model.DataMovieDetails
import jsanzo.movies.data.model.DataMovieGenre
import jsanzo.movies.data.model.DataMovieProductionCompany
import jsanzo.movies.data.model.DataMovieProductionCountry
import jsanzo.movies.data.model.DataMovieResult
import jsanzo.movies.data.model.DataMovieSpokenLanguage
import jsanzo.movies.database.entity.MovieCollectionEntity
import jsanzo.movies.database.entity.MovieDetailsEntity
import jsanzo.movies.database.entity.MovieEntity
import jsanzo.movies.database.entity.MovieGenreEntity
import jsanzo.movies.database.entity.MovieProductionCompanyEntity
import jsanzo.movies.database.entity.MovieProductionCountryEntity
import jsanzo.movies.database.entity.MovieSpokenLanguageEntity

internal val dataMovieResult = DataMovieResult(
    posterPath = "/path.jpg",
    adult = false,
    overview = "overview",
    releaseDate = "2024-01-01",
    genreIds = listOf(1, 2, 3),
    id = 123,
    originalTitle = "original title",
    originalLanguage = "en",
    title = "title",
    backdropPath = "/backdrop.jpg",
    popularity = 8.5,
    voteCount = 1000,
    video = false,
    voteAverage = 7.8,
)

internal val movieEntity = MovieEntity(
    posterPath = "/path.jpg",
    adult = false,
    overview = "overview",
    releaseDate = "2024-01-01",
    genreIds = listOf(1, 2, 3),
    id = 123,
    originalTitle = "original title",
    originalLanguage = "en",
    title = "title",
    backdropPath = "/backdrop.jpg",
    popularity = 8.5,
    voteCount = 1000,
    video = false,
    voteAverage = 7.8,
)

internal val dataMovieDetails = DataMovieDetails(
    adult = false,
    backdropPath = "/backdrop.jpg",
    belongsToCollection = DataMovieCollection(
        id = 1,
        name = "collection",
        posterPath = "/path.jpg",
        backdropPath = "/backdrop.jpg",
    ),
    budget = 1_000_000,
    genres = listOf(DataMovieGenre(1, "genre")),
    homepage = "home",
    id = 123,
    imdbId = "imdb",
    originCountry = listOf("US", "ES"),
    originalLanguage = "en",
    originalTitle = "original title",
    overview = "overview",
    popularity = 8.5,
    posterPath = "/path.jpg",
    productionCompanies = listOf(DataMovieProductionCompany("company", 1, "/logo.jpg", "US")),
    productionCountries = listOf(DataMovieProductionCountry("US", "United States")),
    releaseDate = "2024-01-01",
    revenue = 5_000_000,
    runtime = 120,
    spokenLanguages = listOf(DataMovieSpokenLanguage("english", "en", "English")),
    status = "Released",
    tagline = "tagline",
    title = "title",
    video = false,
    voteAverage = 7.8,
    voteCount = 1000,
)

internal val movieDetailsEntity = MovieDetailsEntity(
    adult = false,
    backdropPath = "/backdrop.jpg",
    belongsToCollection = MovieCollectionEntity(
        id = 1,
        name = "collection",
        posterPath = "/path.jpg",
        backdropPath = "/backdrop.jpg",
    ),
    budget = 1_000_000,
    genres = listOf(MovieGenreEntity(1, "genre")),
    homepage = "home",
    id = 123,
    imdbId = "imdb",
    originCountry = "US,ES",
    originalLanguage = "en",
    originalTitle = "original title",
    overview = "overview",
    popularity = 8.5,
    posterPath = "/path.jpg",
    productionCompanies = listOf(MovieProductionCompanyEntity("company", 1, "/logo.jpg", "US")),
    productionCountries = listOf(MovieProductionCountryEntity("US", "United States")),
    releaseDate = "2024-01-01",
    revenue = 5_000_000,
    runtime = 120,
    spokenLanguages = listOf(MovieSpokenLanguageEntity("english", "en", "English")),
    status = "Released",
    tagline = "tagline",
    title = "title",
    video = false,
    voteAverage = 7.8,
    voteCount = 1000,
)
