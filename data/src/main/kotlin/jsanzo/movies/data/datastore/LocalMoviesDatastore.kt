package jsanzo.movies.data.datastore

import arrow.core.Either
import arrow.core.Option
import jsanzo.movies.data.error.DataError
import jsanzo.movies.data.model.DataMovie
import jsanzo.movies.data.model.DataMovieDetails

interface LocalMoviesDatastore {

    suspend fun saveMovie(dataMovie: DataMovie): Option<DataError>
    suspend fun saveMovieDetails(dataMovieDetails: DataMovieDetails): Option<DataError>
    suspend fun getMovies(): Either<DataError, List<DataMovie>>
    suspend fun getMovieDetails(movieId: Int): Either<DataError, DataMovieDetails>
    suspend fun searchMovies(query: String): Either<DataError, List<DataMovie>>
}
