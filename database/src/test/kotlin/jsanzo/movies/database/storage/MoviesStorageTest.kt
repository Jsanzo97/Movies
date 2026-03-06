package jsanzo.movies.database.storage

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import jsanzo.movies.data.error.ReadingError
import jsanzo.movies.data.error.WritingError
import jsanzo.movies.database.dao.MoviesDao
import jsanzo.movies.database.entity.toMovieDetailsEntity
import jsanzo.movies.database.entity.toMovieEntity
import jsanzo.movies.database.model.dataMovie
import jsanzo.movies.database.model.dataMovieDetails
import jsanzo.movies.database.model.dataMovieResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class MoviesStorageTest {

    private val moviesDao: MoviesDao = mockk()
    private val moviesStorage = MoviesStorage(moviesDao)

    private val movieId = 123

    @Test
    fun `Given movies in database, When getMovies is called, Then data movie is returned`() = runTest {
        coEvery { moviesDao.getMovies() } returns listOf(dataMovieResult.toMovieEntity())

        val result = moviesStorage.getMovies()

        result shouldBe dataMovie.right()
        coVerify(exactly = 1) { moviesDao.getMovies() }
        confirmVerified(moviesDao)
    }

    @Test
    fun `Given a database error, When getMovies is called, Then ReadingError is returned`() = runTest {
        coEvery { moviesDao.getMovies() } throws Exception()

        val result = moviesStorage.getMovies()

        result shouldBe ReadingError.left()
        coVerify(exactly = 1) { moviesDao.getMovies() }
        confirmVerified(moviesDao)
    }

    @Test
    fun `Given a movie id, When getMovieDetails is called, Then data movie details are returned`() = runTest {
        coEvery { moviesDao.getMovieDetails(movieId) } returns dataMovieDetails.toMovieDetailsEntity()

        val result = moviesStorage.getMovieDetails(movieId)

        result shouldBe dataMovieDetails.right()
        coVerify(exactly = 1) { moviesDao.getMovieDetails(movieId) }
        confirmVerified(moviesDao)
    }

    @Test
    fun `Given a database error, When getMovieDetails is called, Then ReadingError is returned`() = runTest {
        coEvery { moviesDao.getMovieDetails(movieId) } throws Exception()

        val result = moviesStorage.getMovieDetails(movieId)

        result shouldBe ReadingError.left()
        coVerify(exactly = 1) { moviesDao.getMovieDetails(movieId) }
        confirmVerified(moviesDao)
    }

    @Test
    fun `Given a movie, When saveMovie is called, Then None is returned`() = runTest {
        coEvery { moviesDao.saveMovie(dataMovieResult.toMovieEntity()) } returns Unit

        val result = moviesStorage.saveMovie(dataMovieResult)

        result shouldBe None
        coVerify(exactly = 1) { moviesDao.saveMovie(dataMovieResult.toMovieEntity()) }
        confirmVerified(moviesDao)
    }

    @Test
    fun `Given a database error, When saveMovie is called, Then WritingError is returned`() = runTest {
        coEvery { moviesDao.saveMovie(any()) } throws Exception()

        val result = moviesStorage.saveMovie(dataMovieResult)

        result shouldBe WritingError.some()
        coVerify(exactly = 1) { moviesDao.saveMovie(dataMovieResult.toMovieEntity()) }
        confirmVerified(moviesDao)
    }

    @Test
    fun `Given movie details, When saveMovieDetails is called, Then None is returned`() = runTest {
        coEvery { moviesDao.saveMovieDetails(dataMovieDetails.toMovieDetailsEntity()) } returns Unit

        val result = moviesStorage.saveMovieDetails(dataMovieDetails)

        result shouldBe None
        coVerify(exactly = 1) { moviesDao.saveMovieDetails(dataMovieDetails.toMovieDetailsEntity()) }
        confirmVerified(moviesDao)
    }

    @Test
    fun `Given a database error, When saveMovieDetails is called, Then WritingError is returned`() = runTest {
        coEvery { moviesDao.saveMovieDetails(any()) } throws Exception()

        val result = moviesStorage.saveMovieDetails(dataMovieDetails)

        result shouldBe WritingError.some()
        coVerify(exactly = 1) { moviesDao.saveMovieDetails(dataMovieDetails.toMovieDetailsEntity()) }
        confirmVerified(moviesDao)
    }

    @Test
    fun `Given a query, When searchMovies is called, Then filtered data movie is returned`() = runTest {
        val query = "harry"
        coEvery { moviesDao.searchMovies(query) } returns listOf(dataMovieResult.toMovieEntity())

        val result = moviesStorage.searchMovies(query)

        result shouldBe dataMovie.right()
        coVerify(exactly = 1) { moviesDao.searchMovies(query) }
        confirmVerified(moviesDao)
    }

    @Test
    fun `Given a database error, When searchMovies is called, Then ReadingError is returned`() = runTest {
        val query = "harry"
        coEvery { moviesDao.searchMovies(query) } throws Exception()

        val result = moviesStorage.searchMovies(query)

        result shouldBe ReadingError.left()
        coVerify(exactly = 1) { moviesDao.searchMovies(query) }
        confirmVerified(moviesDao)
    }
}
