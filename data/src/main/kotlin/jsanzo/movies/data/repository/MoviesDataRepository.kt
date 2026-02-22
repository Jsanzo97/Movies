package jsanzo.movies.data.repository

import arrow.core.Either
import arrow.core.None
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.recover
import arrow.core.right
import arrow.core.some
import jsanzo.movies.data.datastore.LocalMoviesDatastore
import jsanzo.movies.data.datastore.RemoteMoviesDatastore
import jsanzo.movies.data.entity.toDataMovieResult
import jsanzo.movies.data.entity.toMovie
import jsanzo.movies.data.entity.toMovieDetails
import jsanzo.movies.data.error.toMovieError
import jsanzo.movies.domain.entity.Movie
import jsanzo.movies.domain.entity.MovieDetails
import jsanzo.movies.domain.entity.MovieResult
import jsanzo.movies.domain.error.MovieError
import jsanzo.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class MoviesDataRepository(
    private val remoteMoviesDatastore: RemoteMoviesDatastore,
    private val localMoviesDatastore: LocalMoviesDatastore,
    private val dispatcher: CoroutineDispatcher,
) : MoviesRepository {

    override suspend fun getMovies(page: Int): Either<MovieError, Movie> = withContext(dispatcher) {
        remoteMoviesDatastore.getMovies(page)
            .map { it.toMovie() }
            .recover {
                localMoviesDatastore.getMovies()
                    .mapLeft { it.toMovieError() }
                    .map { it.toMovie() }
                    .bind()
            }
    }

    override suspend fun getMovieDetails(movieId: Int): Either<MovieError, MovieDetails> = withContext(dispatcher) {
        remoteMoviesDatastore.getMovieDetails(movieId)
            .flatMap { dataMovieDetails ->
                localMoviesDatastore.saveMovieDetails(dataMovieDetails)
                    .map { error -> error.toMovieError().left() }
                    .getOrElse { dataMovieDetails.toMovieDetails().right() }
            }
            .recover {
                localMoviesDatastore.getMovieDetails(movieId)
                    .mapLeft { it.toMovieError() }
                    .map { it.toMovieDetails() }
                    .bind()
            }
    }

    override suspend fun saveMovie(movie: MovieResult) = withContext(dispatcher) {
        localMoviesDatastore.saveMovie(movie.toDataMovieResult())
            .map { error -> error.toMovieError().some() }
            .getOrElse { None }
    }
}
