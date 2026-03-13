@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package jsanzo.movies.remote.dto.response

import jsanzo.movies.data.model.DataMovieCollection
import jsanzo.movies.data.model.DataMovieDetails
import jsanzo.movies.data.model.DataMovieGenre
import jsanzo.movies.data.model.DataMovieProductionCompany
import jsanzo.movies.data.model.DataMovieProductionCountry
import jsanzo.movies.data.model.DataMovieSpokenLanguage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetMoviesDetailsResponse(
    val adult: Boolean,
    @SerialName("backdrop_path")
    val backdropPath: String?,
    @SerialName("belongs_to_collection")
    val belongsToCollection: GetMoviesDetailCollection?,
    val budget: Int,
    val genres: List<GetMoviesGenre>,
    val homepage: String?,
    val id: Int,
    @SerialName("imdb_id")
    val imdbId: String?,
    @SerialName("origin_country")
    val originCountry: List<String>,
    @SerialName("original_language")
    val originalLanguage: String,
    @SerialName("original_title")
    val originalTitle: String,
    val overview: String?,
    val popularity: Double,
    @SerialName("poster_path")
    val posterPath: String?,
    @SerialName("production_companies")
    val productionCompanies: List<GetMoviesProductionCompany>,
    @SerialName("production_countries")
    val productionCountries: List<GetMoviesProductionCountry>,
    @SerialName("release_date")
    val releaseDate: String,
    val revenue: Long,
    val runtime: Int?,
    @SerialName("spoken_languages")
    val spokenLanguages: List<GetMoviesSpokenLanguage>,
    val status: String,
    val tagline: String?,
    val title: String,
    val video: Boolean,
    @SerialName("vote_average")
    val voteAverage: Double,
    @SerialName("vote_count")
    val voteCount: Int,
)

@Serializable
data class GetMoviesDetailCollection(
    val id: Int,
    val name: String,
    @SerialName("poster_path")
    val posterPath: String,
    @SerialName("backdrop_path")
    val backdropPath: String,
)

@Serializable
data class GetMoviesGenre(
    val id: Int,
    val name: String,
)

@Serializable
data class GetMoviesProductionCompany(
    val name: String,
    val id: Int,
    @SerialName("logo_path")
    val logoPath: String?,
    @SerialName("origin_country")
    val originCountry: String,
)

@Serializable
data class GetMoviesProductionCountry(
    @SerialName("iso_3166_1")
    val iso: String,
    val name: String,
)

@Serializable
data class GetMoviesSpokenLanguage(
    @SerialName("english_name")
    val englishName: String,
    @SerialName("iso_639_1")
    val iso: String,
    val name: String,
)

fun GetMoviesDetailsResponse.toDataMovieDetails() = DataMovieDetails(
    adult,
    "https://image.tmdb.org/t/p/original/$backdropPath",
    belongsToCollection?.toDataMovieCollection(),
    budget,
    genres.map { it.toDataMovieGenre() },
    homepage,
    id,
    imdbId,
    originCountry,
    originalLanguage,
    originalTitle,
    overview,
    popularity,
    "https://image.tmdb.org/t/p/original/$posterPath",
    productionCompanies.map { it.toDataMovieProductionCompany() },
    productionCountries.map { it.toDataMovieProductionCountry() },
    releaseDate,
    revenue,
    runtime,
    spokenLanguages.map { it.toDataMovieSpokenLanguage() },
    status,
    tagline,
    title,
    video,
    voteAverage,
    voteCount,
)

fun GetMoviesDetailCollection.toDataMovieCollection() = DataMovieCollection(
    id,
    name,
    posterPath,
    backdropPath,
)

fun GetMoviesGenre.toDataMovieGenre() = DataMovieGenre(
    id,
    name,
)

fun GetMoviesProductionCompany.toDataMovieProductionCompany() = DataMovieProductionCompany(
    name,
    id,
    logoPath,
    originCountry,
)

fun GetMoviesProductionCountry.toDataMovieProductionCountry() = DataMovieProductionCountry(
    iso,
    name,
)

fun GetMoviesSpokenLanguage.toDataMovieSpokenLanguage() = DataMovieSpokenLanguage(
    englishName,
    iso,
    name,
)
