package jsanzo.movies.data.repository

import arrow.core.*
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

class MoviesDataRepository(
    private val remoteMoviesDatastore: RemoteMoviesDatastore,
    private val localMoviesDatastore: LocalMoviesDatastore,
    private val dispatcher: CoroutineDispatcher
): MoviesRepository {

    override suspend fun getMovies(page: Int): Either<MovieError, Flow<Movie>> = withContext(dispatcher) {
        remoteMoviesDatastore.getMovies(page).fold(
            ifLeft = {
                localMoviesDatastore.getMovies().fold(
                    ifLeft = { error ->
                        error.toMovieError().left()
                    },
                    ifRight = { dataMovieResult ->
                        flowOf(
                            dataMovieResult.toMovie()
                        ).right()
                    }
                )
            },
            ifRight = { dataMovie ->
                flowOf(
                    dataMovie.toMovie()
                ).right()
            }
        )
    }

    override suspend fun getMovieDetails(movieId: Int): Either<MovieError, MovieDetails> = withContext(dispatcher) {
        remoteMoviesDatastore.getMovieDetails(movieId).fold(
            ifLeft = {
                localMoviesDatastore.getMovieDetails(movieId).fold(
                    ifLeft = { error ->
                        error.toMovieError().left()
                    },
                    ifRight = { dataMovieDetails ->
                        dataMovieDetails.toMovieDetails().right()
                    }
                )
             },
            ifRight = { dataMovieDetails ->
                localMoviesDatastore.saveMovieDetails(dataMovieDetails).fold(
                    ifEmpty = {
                        dataMovieDetails.toMovieDetails().right()
                    },
                    ifSome = { error ->
                        error.toMovieError().left()
                    }
                )

            }
        )
    }

    override suspend fun saveMovie(movie: MovieResult) = withContext(dispatcher) {
        localMoviesDatastore.saveMovie(movie.toDataMovieResult()).fold(
            ifSome = { error ->
                error.toMovieError().some()
            },
            ifEmpty = {
                None
            }
        )
    }

}