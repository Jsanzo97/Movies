package jsanzo.movies.data.datastore

import arrow.core.Either
import jsanzo.movies.data.error.DataError
import jsanzo.movies.data.model.DataMovie
import jsanzo.movies.data.model.DataMovieDetails

interface RemoteMoviesDatastore {

    suspend fun getMovies(page: Int): Either<DataError, DataMovie>
    suspend fun getMovieDetails(movieId: Int): Either<DataError, DataMovieDetails>
}
