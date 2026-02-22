package jsanzo.movies.data.datastore

import arrow.core.Either
import arrow.core.Option
import jsanzo.movies.data.error.DataError
import jsanzo.movies.data.model.DataMovie
import jsanzo.movies.data.model.DataMovieDetails
import jsanzo.movies.data.model.DataMovieResult

interface LocalMoviesDatastore {

    suspend fun saveMovie(dataMovie: DataMovieResult): Option<DataError>
    suspend fun saveMovieDetails(dataMovieDetails: DataMovieDetails): Option<DataError>
    suspend fun getMovies(): Either<DataError, DataMovie>
    suspend fun getMovieDetails(movieId: Int): Either<DataError, DataMovieDetails>
}
