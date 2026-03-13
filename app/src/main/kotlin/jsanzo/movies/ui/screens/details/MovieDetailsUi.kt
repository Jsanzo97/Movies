package jsanzo.movies.ui.screens.details

import androidx.compose.runtime.Immutable
import jsanzo.movies.domain.model.DomainMovieDetails

@Immutable
data class MovieDetailsUi(
    val id: Int,
    val title: String,
    val originalTitle: String,
    val originalLanguage: String,
    val overview: String?,
    val tagline: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val status: String,
    val homepage: String?,
    val runtime: Int?,
    val budget: Int,
    val revenue: Long,
    val popularity: Double,
    val voteAverage: Double,
    val voteCount: Int,
    val adult: Boolean,
    val video: Boolean,
    val genres: String,
    val spokenLanguages: String,
    val productionCompanies: String,
    val productionCountries: String,
)

fun DomainMovieDetails.toMovieDetailsUi() = MovieDetailsUi(
    id = id,
    title = title,
    originalTitle = originalTitle,
    originalLanguage = originalLanguage,
    overview = overview,
    tagline = tagline,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    status = status,
    homepage = homepage,
    runtime = runtime,
    budget = budget,
    revenue = revenue,
    popularity = popularity,
    voteAverage = voteAverage,
    voteCount = voteCount,
    adult = adult,
    video = video,
    genres = genres.joinToString(", ") { it.name },
    spokenLanguages = spokenLanguages.joinToString(", ") { it.name },
    productionCompanies = productionCompanies.joinToString(", ") { it.name },
    productionCountries = productionCountries.joinToString(", ") { it.name },
)
