@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package jsanzo.movies.database.entity

import androidx.room.Entity
import jsanzo.movies.data.model.DataMovieCollection
import jsanzo.movies.data.model.DataMovieDetails
import jsanzo.movies.data.model.DataMovieGenre
import jsanzo.movies.data.model.DataMovieProductionCompany
import jsanzo.movies.data.model.DataMovieProductionCountry
import jsanzo.movies.data.model.DataMovieSpokenLanguage
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "Movie details",
    primaryKeys = ["id"],
)
data class MovieDetailsEntity(
    val adult: Boolean,
    val backdropPath: String,
    val belongsToCollection: MovieCollectionEntity?,
    val budget: Int,
    val genres: List<MovieGenreEntity>,
    val homepage: String,
    val id: Int,
    val imdbId: String,
    val originCountry: String,
    val originalLanguage: String,
    val originalTitle: String,
    val overview: String,
    val popularity: Double,
    val posterPath: String,
    val productionCompanies: List<MovieProductionCompanyEntity>,
    val productionCountries: List<MovieProductionCountryEntity>,
    val releaseDate: String,
    val revenue: Long,
    val runtime: Int,
    val spokenLanguages: List<MovieSpokenLanguageEntity>,
    val status: String,
    val tagline: String,
    val title: String,
    val video: Boolean,
    val voteAverage: Double,
    val voteCount: Int,
)

@Serializable
data class MovieCollectionEntity(
    val id: Int,
    val name: String,
    val posterPath: String,
    val backdropPath: String,
)

@Serializable
data class MovieGenreEntity(
    val id: Int,
    val name: String,
)

@Serializable
data class MovieProductionCompanyEntity(
    val name: String,
    val id: Int,
    val logoPath: String?,
    val originCountry: String,
)

@Serializable
data class MovieProductionCountryEntity(
    val iso: String,
    val name: String,
)

@Serializable
data class MovieSpokenLanguageEntity(
    val englishName: String,
    val iso: String,
    val name: String,
)

fun DataMovieDetails.toMovieDetailsEntity() = MovieDetailsEntity(
    adult = adult,
    backdropPath = backdropPath,
    belongsToCollection = belongsToCollection?.toMovieCollectionEntity(),
    budget = budget,
    genres = genres.map { it.toMovieGenreEntity() },
    homepage = homepage,
    id = id,
    imdbId = imdbId,
    originCountry = originCountry.joinToString(","),
    originalLanguage = originalLanguage,
    originalTitle = originalTitle,
    overview = overview,
    popularity = popularity,
    posterPath = posterPath,
    productionCompanies = productionCompanies.map { it.toMovieProductionCompanyEntity() },
    productionCountries = productionCountries.map { it.toMovieProductionCountryEntity() },
    releaseDate = releaseDate,
    revenue = revenue,
    runtime = runtime,
    spokenLanguages = spokenLanguages.map { it.toMovieSpokenLanguageEntity() },
    status = status,
    tagline = tagline,
    title = title,
    video = video,
    voteAverage = voteAverage,
    voteCount = voteCount,
)

fun MovieDetailsEntity.toDataMovieDetails() = DataMovieDetails(
    adult = adult,
    backdropPath = backdropPath,
    belongsToCollection = belongsToCollection?.toDataMovieCollection(),
    budget = budget,
    genres = genres.map { it.toDataMovieGenre() },
    homepage = homepage,
    id = id,
    imdbId = imdbId,
    originCountry = originCountry.split(","),
    originalLanguage = originalLanguage,
    originalTitle = originalTitle,
    overview = overview,
    popularity = popularity,
    posterPath = posterPath,
    productionCompanies = productionCompanies.map { it.toDataMovieCollectionEntity() },
    productionCountries = productionCountries.map { it.toDataMovieProductionCountry() },
    releaseDate = releaseDate,
    revenue = revenue,
    runtime = runtime,
    spokenLanguages = spokenLanguages.map { it.toDataMovieSpokenLanguage() },
    status = status,
    tagline = tagline,
    title = title,
    video = video,
    voteAverage = voteAverage,
    voteCount = voteCount,
)

fun DataMovieCollection.toMovieCollectionEntity() = MovieCollectionEntity(
    id,
    name,
    posterPath,
    backdropPath,
)

fun MovieCollectionEntity.toDataMovieCollection() = DataMovieCollection(
    id,
    name,
    posterPath,
    backdropPath,
)

fun DataMovieGenre.toMovieGenreEntity() = MovieGenreEntity(
    id,
    name,
)

fun MovieGenreEntity.toDataMovieGenre() = DataMovieGenre(
    id,
    name,
)

fun DataMovieProductionCompany.toMovieProductionCompanyEntity() = MovieProductionCompanyEntity(
    name,
    id,
    logoPath,
    originCountry,
)

fun MovieProductionCompanyEntity.toDataMovieCollectionEntity() = DataMovieProductionCompany(
    name,
    id,
    logoPath,
    originCountry,
)

fun DataMovieProductionCountry.toMovieProductionCountryEntity() = MovieProductionCountryEntity(
    iso,
    name,
)

fun MovieProductionCountryEntity.toDataMovieProductionCountry() = DataMovieProductionCountry(
    iso,
    name,
)

fun DataMovieSpokenLanguage.toMovieSpokenLanguageEntity() = MovieSpokenLanguageEntity(
    englishName,
    iso,
    name,
)

fun MovieSpokenLanguageEntity.toDataMovieSpokenLanguage() = DataMovieSpokenLanguage(
    englishName,
    iso,
    name,
)
