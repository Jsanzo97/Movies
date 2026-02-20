package jsanzo.movies.domain.entity

data class MovieDetails(
    val adult: Boolean,
    val backdropPath: String?,
    val belongsToCollection: MovieCollection?,
    val budget: Int,
    val genres: List<MovieGenre>,
    val homepage: String?,
    val id: Int,
    val imdbId: String?,
    val originalLanguage: String,
    val originalTitle: String,
    val overview: String?,
    val popularity: Double,
    val posterPath: String?,
    val productionCompanies: List<MovieProductionCompany>,
    val productionCountries: List<MovieProductionCountry>,
    val releaseDate: String,
    val revenue: Int,
    val runtime: Int?,
    val spokenLanguages: List<MovieSpokenLanguage>,
    val status: String,
    val tagline: String?,
    val title: String,
    val video: Boolean,
    val voteAverage: Double,
    val voteCount: Int
)

