@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package jsanzo.movies.database.entity

import androidx.room.Entity
import jsanzo.movies.data.model.DataMovie
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "Movies",
    primaryKeys = ["id"],
)
data class MovieEntity(
    val posterPath: String,
    val adult: Boolean,
    val overview: String,
    val releaseDate: String,
    val genreIds: List<Int>,
    val id: Int,
    val originalTitle: String,
    val originalLanguage: String,
    val title: String,
    val backdropPath: String,
    val popularity: Double,
    val voteCount: Int,
    val video: Boolean,
    val voteAverage: Double,
)

fun DataMovie.toMovieEntity() = MovieEntity(
    posterPath = posterPath,
    adult = adult,
    overview = overview,
    releaseDate = releaseDate,
    genreIds = genreIds,
    id = id,
    originalTitle = originalTitle,
    originalLanguage = originalLanguage,
    title = title,
    backdropPath = backdropPath,
    popularity = popularity,
    voteCount = voteCount,
    video = video,
    voteAverage = voteAverage,
)

fun List<MovieEntity>.toDataMovie() = map { it.toDataMovie() }

private fun MovieEntity.toDataMovie() = DataMovie(
    posterPath = posterPath,
    adult = adult,
    overview = overview,
    releaseDate = releaseDate,
    genreIds = genreIds,
    id = id,
    originalTitle = originalTitle,
    originalLanguage = originalLanguage,
    title = title,
    backdropPath = backdropPath,
    popularity = popularity,
    voteCount = voteCount,
    video = video,
    voteAverage = voteAverage,
)
