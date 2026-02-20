package jsanzo.movies.data.entity

import jsanzo.movies.domain.entity.MovieDetails

data class DataMovieDetails(
    val adult: Boolean,
    val backdropPath: String?,
    val belongsToCollection: DataMovieCollection?,
    val budget: Int,
    val genres: List<DataMovieGenre>,
    val homepage: String?,
    val id: Int,
    val imdbId: String?,
    val originalLanguage: String,
    val originalTitle: String,
    val overview: String?,
    val popularity: Double,
    val posterPath: String?,
    val productionCompanies: List<DataMovieProductionCompany>,
    val productionCountries: List<DataMovieProductionCountry>,
    val releaseDate: String,
    val revenue: Int,
    val runtime: Int?,
    val spokenLanguages: List<DataMovieSpokenLanguage>,
    val status: String,
    val tagline: String?,
    val title: String,
    val video: Boolean,
    val voteAverage: Double,
    val voteCount: Int
)

fun DataMovieDetails.toMovieDetails() = MovieDetails(
    adult,
    backdropPath,
    belongsToCollection?.toMovieCollection(),
    budget,
    genres.map { it.toMovieGenre() },
    homepage,
    id,
    imdbId,
    originalLanguage,
    originalTitle,
    overview,
    popularity,
    posterPath,
    productionCompanies.map { it.toMovieProductionCompany() },
    productionCountries.map { it.toMovieProductionCountry() },
    releaseDate,
    revenue,
    runtime,
    spokenLanguages.map { it.toMovieSpokenLanguage() },
    status,
    tagline,
    title,
    video,
    voteAverage,
    voteCount
)

