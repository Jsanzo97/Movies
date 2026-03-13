package jsanzo.movies.domain.repository

import arrow.core.Either
import arrow.core.Option
import jsanzo.movies.domain.error.MovieError
import jsanzo.movies.domain.model.DomainMovie
import jsanzo.movies.domain.model.DomainMovieDetails

interface MoviesRepository {

    suspend fun getMovies(page: Int): Either<MovieError, List<DomainMovie>>
    suspend fun getMovieDetails(movieId: Int): Either<MovieError, DomainMovieDetails>
    suspend fun saveMovie(movie: DomainMovie): Option<MovieError>
    suspend fun searchMovies(query: String): Either<MovieError, List<DomainMovie>>
}
