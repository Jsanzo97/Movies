package jsanzo.movies.data.repository

import arrow.core.Either
import arrow.core.None
import arrow.core.getOrElse
import arrow.core.recover
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
import jsanzo.movies.domain.utils.onSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class MoviesDataRepository(
    private val remoteMoviesDatastore: RemoteMoviesDatastore,
    private val localMoviesDatastore: LocalMoviesDatastore,
    private val dispatcher: CoroutineDispatcher,
) : MoviesRepository {

    override suspend fun getMovies(page: Int): Either<MovieError, List<DomainMovie>> = withContext(dispatcher) {
        remoteMoviesDatastore.getMovies(page)
            .recover { remoteError ->
                localMoviesDatastore.getMovies()
                    .mapLeft { remoteError.toMovieError() }
                    .bind()
            }
            .map { it.toDomainMovie() }
    }

    override suspend fun getMovieDetails(movieId: Int): Either<MovieError, DomainMovieDetails> = withContext(dispatcher) {
        localMoviesDatastore.getMovieDetails(movieId)
            .recover { localError ->
                remoteMoviesDatastore.getMovieDetails(movieId)
                    .onSuccess { movieDetails -> localMoviesDatastore.saveMovieDetails(movieDetails) }
                    .mapLeft { remoteError -> remoteError.toMovieError() }
                    .bind()
            }
            .map { it.toMovieDetails() }
    }

    override suspend fun saveMovie(movie: DomainMovie) = withContext(dispatcher) {
        localMoviesDatastore.saveMovie(movie.toDataMovie())
            .map { error -> error.toMovieError().some() }
            .getOrElse { None }
    }

    override suspend fun searchMovies(query: String): Either<MovieError, List<DomainMovie>> = withContext(dispatcher) {
        remoteMoviesDatastore.searchMovies(query)
            .recover { remoteError ->
                localMoviesDatastore.searchMovies(query)
                    .mapLeft { remoteError.toMovieError() }
                    .bind()
            }
            .map { it.toDomainMovie() }
    }
}
