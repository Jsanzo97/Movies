package jsanzo.movies.data.model

import jsanzo.movies.domain.model.DomainMovie

data class DataMovie(
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

fun List<DataMovie>.toDomainMovie() = map { it.toDomainMovie() }

private fun DataMovie.toDomainMovie() = DomainMovie(
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

fun DomainMovie.toDataMovie() = DataMovie(
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
