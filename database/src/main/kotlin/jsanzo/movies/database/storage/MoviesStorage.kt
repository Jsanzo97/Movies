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
import jsanzo.movies.database.dao.MoviesDao
import jsanzo.movies.database.entity.toDataMovie
import jsanzo.movies.database.entity.toDataMovieDetails
import jsanzo.movies.database.entity.toMovieDetailsEntity
import jsanzo.movies.database.entity.toMovieEntity

class MoviesStorage(
    private val moviesDao: MoviesDao,
) : LocalMoviesDatastore {

    override suspend fun getMovies(): Either<DataError, List<DataMovie>> {
        return try {
            moviesDao.getMovies().toDataMovie().right()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            ReadingError.left()
        }
    }

    override suspend fun getMovieDetails(movieId: Int): Either<DataError, DataMovieDetails> {
        return try {
            moviesDao.getMovieDetails(movieId).toDataMovieDetails().right()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            ReadingError.left()
        }
    }

    override suspend fun saveMovie(dataMovie: DataMovie): Option<DataError> {
        return try {
            moviesDao.saveMovie(dataMovie.toMovieEntity())
            None
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            WritingError.some()
        }
    }

    override suspend fun saveMovieDetails(dataMovieDetails: DataMovieDetails): Option<DataError> {
        return try {
            moviesDao.saveMovieDetails(dataMovieDetails.toMovieDetailsEntity())
            None
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            WritingError.some()
        }
    }

    override suspend fun searchMovies(query: String): Either<DataError, List<DataMovie>> {
        return try {
            moviesDao.searchMovies(query).toDataMovie().right()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (_: Exception) {
            ReadingError.left()
        }
    }
}
