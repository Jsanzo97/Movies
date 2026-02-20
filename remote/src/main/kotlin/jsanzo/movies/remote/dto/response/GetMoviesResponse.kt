package jsanzo.movies.remote.dto.response

import jsanzo.movies.data.entity.DataMovie
import jsanzo.movies.data.entity.DataMovieResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetMoviesResponse(
    val page: Int,
    val results: List<MoviesResponseResult>,
    @SerialName("total_results")
    val totalResults: Int,
    @SerialName("total_pages")
    val totalPages: Int
)

@Serializable
data class MoviesResponseResult(
    @SerialName("poster_path")
    val posterPath: String?,
    val adult: Boolean,
    val overview: String,
    @SerialName("release_date")
    val releaseDate: String,
    @SerialName("genre_ids")
    val genreIds: List<Int>,
    val id: Int,
    @SerialName("original_title")
    val originalTitle: String,
    @SerialName("original_language")
    val originalLanguage: String,
    val title: String,
    @SerialName("backdrop_path")
    val backdropPath: String?,
    val popularity: Double,
    @SerialName("vote_count")
    val voteCount: Int,
    val video: Boolean,
    @SerialName("vote_average")
    val voteAverage: Double
)

fun GetMoviesResponse.toDataMovie() = DataMovie(
    page,
    results.map { it.toDataMovie() },
    totalResults,
    totalPages
)

fun MoviesResponseResult.toDataMovie() = DataMovieResult(
    posterPath,
    adult,
    overview,
    releaseDate,
    genreIds,
    id,
    originalTitle,
    originalLanguage,
    title,
    backdropPath,
    popularity,
    voteCount,
    video,
    voteAverage
)
