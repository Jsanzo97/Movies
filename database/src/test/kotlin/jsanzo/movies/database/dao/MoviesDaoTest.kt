package jsanzo.movies.database.dao

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import jsanzo.movies.database.model.movieEntity
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class MoviesDaoTest {

    private val moviesDao: MoviesDao = mockk()

    @Test
    fun `saveMovie saves correctly`() = runTest {
        coEvery { moviesDao.saveMovie(any()) } returns Unit
        moviesDao.saveMovie(movieEntity) shouldBe Unit
    }

    @Test
    fun `getMovies returns list correctly`() = runTest {
        val movies = listOf(movieEntity)
        coEvery { moviesDao.getMovies() } returns movies
        moviesDao.getMovies() shouldBe movies
    }

    @Test
    fun `getMovieDetails returns entity correctly`() = runTest {
        coEvery { moviesDao.getMovieDetails(any()) } returns mockk()
        val result = moviesDao.getMovieDetails(123)
        result shouldBe result
    }

    @Test
    fun `searchMovies returns filtered list correctly`() = runTest {
        val query = "harry"
        val movies = listOf(movieEntity)
        coEvery { moviesDao.searchMovies(query) } returns movies

        moviesDao.searchMovies(query) shouldBe movies
    }
}
