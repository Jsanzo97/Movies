package jsanzo.movies.database.storage

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import jsanzo.movies.data.error.ReadingError
import jsanzo.movies.data.error.WritingError
import jsanzo.movies.data.model.DataMovie
import jsanzo.movies.database.dao.MoviesDao
import jsanzo.movies.database.entity.toDataMovieDetails
import jsanzo.movies.database.entity.toDataMovieResult
import jsanzo.movies.database.entity.toMovieDetailsEntity
import jsanzo.movies.database.entity.toMovieEntity
import jsanzo.movies.database.model.dataMovieDetails
import jsanzo.movies.database.model.dataMovieResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class MoviesStorageTest {

    private val moviesDao: MoviesDao = mockk()
    private val moviesStorage = MoviesStorage(moviesDao)

    private val movieId = 123

    @Test
    fun `getMovies returns data movie on success`() = runTest {
        val movieEntities = listOf(dataMovieResult.toMovieEntity())
        coEvery { moviesDao.getMovies() } returns movieEntities

        val result = moviesStorage.getMovies()

        result shouldBe DataMovie(
            0,
            movieEntities.map { it.toDataMovieResult() },
            0,
            0,
        ).right()
    }

    @Test
    fun `getMovies returns reading error on failure`() = runTest {
        coEvery { moviesDao.getMovies() } throws Exception()

        val result = moviesStorage.getMovies()

        result shouldBe ReadingError.left()
    }

    @Test
    fun `getMovieDetails returns data movie details on success`() = runTest {
        val movieDetailsEntity = dataMovieDetails.toMovieDetailsEntity()
        coEvery { moviesDao.getMovieDetails(movieId) } returns movieDetailsEntity

        val result = moviesStorage.getMovieDetails(movieId)

        result shouldBe movieDetailsEntity.toDataMovieDetails().right()
    }

    @Test
    fun `getMovieDetails returns reading error on failure`() = runTest {
        coEvery { moviesDao.getMovieDetails(movieId) } throws Exception()

        val result = moviesStorage.getMovieDetails(movieId)

        result shouldBe ReadingError.left()
    }

    @Test
    fun `saveMovie returns none on success`() = runTest {
        coEvery { moviesDao.saveMovie(any()) } returns Unit

        val result = moviesStorage.saveMovie(dataMovieResult)

        result shouldBe None
        coVerify { moviesDao.saveMovie(dataMovieResult.toMovieEntity()) }
    }

    @Test
    fun `saveMovie returns writing error on failure`() = runTest {
        coEvery { moviesDao.saveMovie(any()) } throws Exception()

        val result = moviesStorage.saveMovie(dataMovieResult)

        result shouldBe WritingError.some()
    }

    @Test
    fun `saveMovieDetails returns none on success`() = runTest {
        coEvery { moviesDao.saveMovieDetails(any()) } returns Unit

        val result = moviesStorage.saveMovieDetails(dataMovieDetails)

        result shouldBe None
        coVerify { moviesDao.saveMovieDetails(dataMovieDetails.toMovieDetailsEntity()) }
    }

    @Test
    fun `saveMovieDetails returns reading error on failure`() = runTest {
        coEvery { moviesDao.saveMovieDetails(any()) } throws Exception()

        val result = moviesStorage.saveMovieDetails(dataMovieDetails)

        result shouldBe WritingError.some()
    }

    @Test
    fun `searchMovies returns data movie with filtered results on success`() = runTest {
        val query = "harry"
        val movieEntities = listOf(dataMovieResult.toMovieEntity())
        coEvery { moviesDao.searchMovies(query) } returns movieEntities

        val result = moviesStorage.searchMovies(query)

        result shouldBe DataMovie(
            0,
            movieEntities.map { it.toDataMovieResult() },
            0,
            0,
        ).right()
    }

    @Test
    fun `searchMovies returns reading error on failure`() = runTest {
        val query = "harry"
        coEvery { moviesDao.searchMovies(query) } throws Exception()

        val result = moviesStorage.searchMovies(query)

        result shouldBe ReadingError.left()
    }
}
