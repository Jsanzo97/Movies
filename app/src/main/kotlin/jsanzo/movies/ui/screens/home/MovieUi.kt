package jsanzo.movies.ui.screens.home

import androidx.compose.runtime.Immutable
import jsanzo.movies.domain.model.DomainMovie
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
data class MovieUi(
    val id: Int,
    val title: String,
    val originalTitle: String,
    val originalLanguage: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val popularity: Double,
    val voteAverage: Double,
    val voteCount: Int,
    val adult: Boolean,
    val video: Boolean,
    val genreIds: ImmutableList<Int>,
)

fun List<DomainMovie>.toMovieUi() = map { it.toMovieUi() }.toImmutableList()

private fun DomainMovie.toMovieUi() = MovieUi(
    id = id,
    title = title,
    originalTitle = originalTitle,
    originalLanguage = originalLanguage,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    popularity = popularity,
    voteAverage = voteAverage,
    voteCount = voteCount,
    adult = adult,
    video = video,
    genreIds = genreIds.toImmutableList(),
)

fun MovieUi.toDomainMovieResult() = DomainMovie(
    id = id,
    title = title,
    originalTitle = originalTitle,
    originalLanguage = originalLanguage,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    popularity = popularity,
    voteAverage = voteAverage,
    voteCount = voteCount,
    adult = adult,
    video = video,
    genreIds = genreIds,
)
