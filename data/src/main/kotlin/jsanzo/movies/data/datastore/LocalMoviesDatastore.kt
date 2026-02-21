package jsanzo.movies.data.datastore

import arrow.core.Either
import arrow.core.Option
import jsanzo.movies.data.entity.DataMovie
import jsanzo.movies.data.entity.DataMovieDetails
import jsanzo.movies.data.entity.DataMovieResult
import jsanzo.movies.data.error.LocalDataError

interface LocalMoviesDatastore {

    suspend fun saveMovie(dataMovie: DataMovieResult): Option<LocalDataError>
    suspend fun saveMovieDetails(dataMovieDetails: DataMovieDetails): Option<LocalDataError>
    suspend fun getMovies(): Either<LocalDataError, DataMovie>
    suspend fun getMovieDetails(movieId: Int): Either<LocalDataError, DataMovieDetails>
}
