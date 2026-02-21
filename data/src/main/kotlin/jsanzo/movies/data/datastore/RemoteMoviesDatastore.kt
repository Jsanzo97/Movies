package jsanzo.movies.data.datastore

import arrow.core.Either
import jsanzo.movies.data.entity.DataMovie
import jsanzo.movies.data.entity.DataMovieDetails
import jsanzo.movies.data.error.RemoteDataError

interface RemoteMoviesDatastore {

    suspend fun getMovies(page: Int): Either<RemoteDataError, DataMovie>
    suspend fun getMovieDetails(movieId: Int): Either<RemoteDataError, DataMovieDetails>
}
