package jsanzo.movies.database.storage

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.left
import arrow.core.right
import arrow.core.some
import jsanzo.movies.data.datastore.LocalMoviesDatastore
import jsanzo.movies.data.error.DataError
import jsanzo.movies.data.error.ReadingError
import jsanzo.movies.data.error.WritingError
import jsanzo.movies.data.model.DataMovie
import jsanzo.movies.data.model.DataMovieDetails
import jsanzo.movies.data.model.DataMovieResult
import jsanzo.movies.database.dao.MoviesDao
import jsanzo.movies.database.entity.toDataMovieDetails
import jsanzo.movies.database.entity.toDataMovieResult
import jsanzo.movies.database.entity.toMovieDetailsEntity
import jsanzo.movies.database.entity.toMovieEntity

class MoviesStorage(
    private val moviesDao: MoviesDao,
) : LocalMoviesDatastore {

    override suspend fun getMovies(): Either<DataError, DataMovie> {
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

    override suspend fun getMovieDetails(movieId: Int): Either<DataError, DataMovieDetails> {
        return try {
            moviesDao.getMovieDetails(movieId).toDataMovieDetails().right()
        } catch (_: Exception) {
            ReadingError.left()
        }
    }

    override suspend fun saveMovie(dataMovie: DataMovieResult): Option<DataError> {
        return try {
            moviesDao.saveMovie(dataMovie.toMovieEntity())
            None
        } catch (_: Exception) {
            WritingError.some()
        }
    }

    override suspend fun saveMovieDetails(dataMovieDetails: DataMovieDetails): Option<DataError> {
        return try {
            moviesDao.saveMovieDetails(dataMovieDetails.toMovieDetailsEntity())
            None
        } catch (_: Exception) {
            WritingError.some()
        }
    }

    override suspend fun searchMovies(query: String): Either<DataError, DataMovie> {
        return try {
            DataMovie(
                0,
                moviesDao.searchMovies(query).map { it.toDataMovieResult() },
                0,
                0,
            ).right()
        } catch (_: Exception) {
            ReadingError.left()
        }
    }
}
