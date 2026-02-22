package jsanzo.movies.domain.repository

import arrow.core.Either
import arrow.core.Option
import jsanzo.movies.domain.entity.Movie
import jsanzo.movies.domain.entity.MovieDetails
import jsanzo.movies.domain.entity.MovieResult
import jsanzo.movies.domain.error.MovieError
import kotlinx.coroutines.flow.Flow

interface MoviesRepository {

    suspend fun getMovies(page: Int): Either<MovieError, Movie>
    suspend fun getMovieDetails(movieId: Int): Either<MovieError, MovieDetails>
    suspend fun saveMovie(movie: MovieResult): Option<MovieError>
}
