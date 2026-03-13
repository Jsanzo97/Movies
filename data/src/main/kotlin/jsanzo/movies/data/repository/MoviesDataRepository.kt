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
import jsanzo.movies.data.error.toMovieError
import jsanzo.movies.data.model.toDataMovie
import jsanzo.movies.data.model.toDomainMovie
import jsanzo.movies.data.model.toMovieDetails
import jsanzo.movies.domain.error.MovieError
import jsanzo.movies.domain.model.DomainMovie
import jsanzo.movies.domain.model.DomainMovieDetails
import jsanzo.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class MoviesDataRepository(
    private val remoteMoviesDatastore: RemoteMoviesDatastore,
    private val localMoviesDatastore: LocalMoviesDatastore,
    private val dispatcher: CoroutineDispatcher,
) : MoviesRepository {

    override suspend fun getMovies(page: Int): Either<MovieError, List<DomainMovie>> = withContext(dispatcher) {
        remoteMoviesDatastore.getMovies(page)
            .map { it.toDomainMovie() }
            .recover {
                localMoviesDatastore.getMovies()
                    .mapLeft { it.toMovieError() }
                    .map { it.toDomainMovie() }
                    .bind()
            }
    }

    override suspend fun getMovieDetails(movieId: Int): Either<MovieError, DomainMovieDetails> = withContext(dispatcher) {
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

    override suspend fun saveMovie(movie: DomainMovie) = withContext(dispatcher) {
        localMoviesDatastore.saveMovie(movie.toDataMovie())
            .map { error -> error.toMovieError().some() }
            .getOrElse { None }
    }

    override suspend fun searchMovies(query: String): Either<MovieError, List<DomainMovie>> = withContext(dispatcher) {
        remoteMoviesDatastore.searchMovies(query)
            .map { it.toDomainMovie() }
            .recover {
                localMoviesDatastore.searchMovies(query)
                    .mapLeft { it.toMovieError() }
                    .map { it.toDomainMovie() }
                    .bind()
            }
    }
}
