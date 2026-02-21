package jsanzo.movies.database.storage

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.left
import arrow.core.right
import arrow.core.some
import jsanzo.movies.data.datastore.LocalMoviesDatastore
import jsanzo.movies.data.entity.DataMovie
import jsanzo.movies.data.entity.DataMovieDetails
import jsanzo.movies.data.entity.DataMovieResult
import jsanzo.movies.data.error.LocalDataError
import jsanzo.movies.data.error.ReadingError
import jsanzo.movies.data.error.WritingError
import jsanzo.movies.database.dao.MoviesDao
import jsanzo.movies.database.entity.toDataMovieDetails
import jsanzo.movies.database.entity.toDataMovieResult
import jsanzo.movies.database.entity.toMovieDetailsEntity
import jsanzo.movies.database.entity.toMovieEntity

class MoviesStorage(
    private val moviesDao: MoviesDao,
) : LocalMoviesDatastore {

    override suspend fun getMovies(): Either<LocalDataError, DataMovie> {
        return try {
            DataMovie(
                0,
                moviesDao.getMovies().map { it.toDataMovieResult() },
                0,
                0,
            ).right()
        } catch (_: Exception) {
            ReadingError.left()
        }
    }

    override suspend fun getMovieDetails(movieId: Int): Either<LocalDataError, DataMovieDetails> {
        return try {
            moviesDao.getMovieDetails(movieId).toDataMovieDetails().right()
        } catch (_: Exception) {
            ReadingError.left()
        }
    }

    override suspend fun saveMovie(dataMovie: DataMovieResult): Option<LocalDataError> {
        return try {
            moviesDao.saveMovie(dataMovie.toMovieEntity())
            None
        } catch (_: Exception) {
            WritingError.some()
        }
    }

    override suspend fun saveMovieDetails(dataMovieDetails: DataMovieDetails): Option<LocalDataError> {
        return try {
            moviesDao.saveMovieDetails(dataMovieDetails.toMovieDetailsEntity())
            None
        } catch (_: Exception) {
            ReadingError.some()
        }
    }
}
