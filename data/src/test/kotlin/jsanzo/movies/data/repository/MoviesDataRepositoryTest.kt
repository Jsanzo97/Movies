package jsanzo.movies.data.repository

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import jsanzo.movies.data.datastore.LocalMoviesDatastore
import jsanzo.movies.data.datastore.RemoteMoviesDatastore
import jsanzo.movies.data.error.NotFound
import jsanzo.movies.data.error.ReadingError
import jsanzo.movies.data.error.WritingError
import jsanzo.movies.data.error.toMovieError
import jsanzo.movies.data.model.dataMovie
import jsanzo.movies.data.model.dataMovieDetails
import jsanzo.movies.data.model.domainMovieResult
import jsanzo.movies.data.model.toDataMovieResult
import jsanzo.movies.data.model.toMovie
import jsanzo.movies.data.model.toMovieDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class MoviesDataRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    private val remoteMoviesDatastore: RemoteMoviesDatastore = mockk()
    private val localMoviesDatastore: LocalMoviesDatastore = mockk()

    private lateinit var repository: MoviesDataRepository

    private val validPage = 1
    private val invalidPage = -1
    private val movieId = 1

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = MoviesDataRepository(remoteMoviesDatastore, localMoviesDatastore, testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given valid page, When remote call succeeds, Then remote data is returned`() = runTest {
        coEvery { remoteMoviesDatastore.getMovies(validPage) } returns dataMovie.right()

        val result = repository.getMovies(validPage)

        result shouldBe dataMovie.toMovie().right()
        coVerify(exactly = 1) { remoteMoviesDatastore.getMovies(validPage) }
        confirmVerified(remoteMoviesDatastore, localMoviesDatastore)
    }

    @Test
    fun `Given valid page, When remote call fails, Then local data is returned`() = runTest {
        coEvery { remoteMoviesDatastore.getMovies(validPage) } returns NotFound.left()
        coEvery { localMoviesDatastore.getMovies() } returns dataMovie.right()

        val result = repository.getMovies(validPage)

        result shouldBe dataMovie.toMovie().right()
        coVerify(exactly = 1) { remoteMoviesDatastore.getMovies(validPage) }
        coVerify(exactly = 1) { localMoviesDatastore.getMovies() }
        confirmVerified(remoteMoviesDatastore, localMoviesDatastore)
    }

    @Test
    fun `Given valid page, When both remote and local fail, Then error is returned`() = runTest {
        coEvery { remoteMoviesDatastore.getMovies(validPage) } returns NotFound.left()
        coEvery { localMoviesDatastore.getMovies() } returns ReadingError.left()

        val result = repository.getMovies(validPage)

        result shouldBe ReadingError.toMovieError().left()
        coVerify(exactly = 1) { remoteMoviesDatastore.getMovies(validPage) }
        coVerify(exactly = 1) { localMoviesDatastore.getMovies() }
        confirmVerified(remoteMoviesDatastore, localMoviesDatastore)
    }

    @Test
    fun `Given invalid page, When both remote and local fail, Then error is returned`() = runTest {
        coEvery { remoteMoviesDatastore.getMovies(invalidPage) } returns NotFound.left()
        coEvery { localMoviesDatastore.getMovies() } returns ReadingError.left()

        val result = repository.getMovies(invalidPage)

        result shouldBe ReadingError.toMovieError().left()
        coVerify(exactly = 1) { remoteMoviesDatastore.getMovies(invalidPage) }
        coVerify(exactly = 1) { localMoviesDatastore.getMovies() }
        confirmVerified(remoteMoviesDatastore, localMoviesDatastore)
    }

    @Test
    fun `Given a movie id, When remote call succeeds and local save succeeds, Then remote data is returned`() = runTest {
        coEvery { remoteMoviesDatastore.getMovieDetails(movieId) } returns dataMovieDetails.right()
        coEvery { localMoviesDatastore.saveMovieDetails(dataMovieDetails) } returns None

        val result = repository.getMovieDetails(movieId)

        result shouldBe dataMovieDetails.toMovieDetails().right()
        coVerify(exactly = 1) { remoteMoviesDatastore.getMovieDetails(movieId) }
        coVerify(exactly = 1) { localMoviesDatastore.saveMovieDetails(dataMovieDetails) }
        confirmVerified(remoteMoviesDatastore, localMoviesDatastore)
    }

    @Test
    fun `Given a movie id, When remote succeeds but local save fails, Then error is returned`() = runTest {
        coEvery { remoteMoviesDatastore.getMovieDetails(movieId) } returns dataMovieDetails.right()
        coEvery { localMoviesDatastore.saveMovieDetails(dataMovieDetails) } returns WritingError.some()
        coEvery { localMoviesDatastore.getMovieDetails(movieId) } returns ReadingError.left()

        val result = repository.getMovieDetails(movieId)

        result shouldBe ReadingError.toMovieError().left()
        coVerify(exactly = 1) { remoteMoviesDatastore.getMovieDetails(movieId) }
        coVerify(exactly = 1) { localMoviesDatastore.saveMovieDetails(dataMovieDetails) }
        coVerify(exactly = 1) { localMoviesDatastore.getMovieDetails(movieId) }
        confirmVerified(remoteMoviesDatastore, localMoviesDatastore)
    }

    @Test
    fun `Given a movie id, When remote call fails, Then local data is returned`() = runTest {
        coEvery { remoteMoviesDatastore.getMovieDetails(movieId) } returns NotFound.left()
        coEvery { localMoviesDatastore.getMovieDetails(movieId) } returns dataMovieDetails.right()

        val result = repository.getMovieDetails(movieId)

        result shouldBe dataMovieDetails.toMovieDetails().right()
        coVerify(exactly = 1) { remoteMoviesDatastore.getMovieDetails(movieId) }
        coVerify(exactly = 1) { localMoviesDatastore.getMovieDetails(movieId) }
        confirmVerified(remoteMoviesDatastore, localMoviesDatastore)
    }

    @Test
    fun `Given a movie id, When both remote and local fail, Then error is returned`() = runTest {
        coEvery { remoteMoviesDatastore.getMovieDetails(movieId) } returns NotFound.left()
        coEvery { localMoviesDatastore.getMovieDetails(movieId) } returns ReadingError.left()

        val result = repository.getMovieDetails(movieId)

        result shouldBe ReadingError.toMovieError().left()
        coVerify(exactly = 1) { remoteMoviesDatastore.getMovieDetails(movieId) }
        coVerify(exactly = 1) { localMoviesDatastore.getMovieDetails(movieId) }
        confirmVerified(remoteMoviesDatastore, localMoviesDatastore)
    }

    @Test
    fun `Given a movie, When local save succeeds, Then None is returned`() = runTest {
        coEvery { localMoviesDatastore.saveMovie(domainMovieResult.toDataMovieResult()) } returns None

        val result = repository.saveMovie(domainMovieResult)

        result shouldBe None
        coVerify(exactly = 1) { localMoviesDatastore.saveMovie(domainMovieResult.toDataMovieResult()) }
        confirmVerified(localMoviesDatastore)
    }

    @Test
    fun `Given a movie, When local save fails, Then error is returned`() = runTest {
        coEvery { localMoviesDatastore.saveMovie(domainMovieResult.toDataMovieResult()) } returns WritingError.some()

        val result = repository.saveMovie(domainMovieResult)

        result shouldBe WritingError.toMovieError().some()
        coVerify(exactly = 1) { localMoviesDatastore.saveMovie(domainMovieResult.toDataMovieResult()) }
        confirmVerified(localMoviesDatastore)
    }

    @Test
    fun `Given a query, When remote call succeeds, Then remote data is returned`() = runTest {
        val query = "harry potter"
        coEvery { remoteMoviesDatastore.searchMovies(query) } returns dataMovie.right()

        val result = repository.searchMovies(query)

        result shouldBe dataMovie.toMovie().right()
        coVerify(exactly = 1) { remoteMoviesDatastore.searchMovies(query) }
        confirmVerified(remoteMoviesDatastore, localMoviesDatastore)
    }

    @Test
    fun `Given a query, When remote call fails, Then local data is returned`() = runTest {
        val query = "harry potter"
        coEvery { remoteMoviesDatastore.searchMovies(query) } returns NotFound.left()
        coEvery { localMoviesDatastore.searchMovies(query) } returns dataMovie.right()

        val result = repository.searchMovies(query)

        result shouldBe dataMovie.toMovie().right()
        coVerify(exactly = 1) { remoteMoviesDatastore.searchMovies(query) }
        coVerify(exactly = 1) { localMoviesDatastore.searchMovies(query) }
        confirmVerified(remoteMoviesDatastore, localMoviesDatastore)
    }

    @Test
    fun `Given a query, When both remote and local fail, Then error is returned`() = runTest {
        val query = "harry potter"
        coEvery { remoteMoviesDatastore.searchMovies(query) } returns NotFound.left()
        coEvery { localMoviesDatastore.searchMovies(query) } returns ReadingError.left()

        val result = repository.searchMovies(query)

        result shouldBe ReadingError.toMovieError().left()
        coVerify(exactly = 1) { remoteMoviesDatastore.searchMovies(query) }
        coVerify(exactly = 1) { localMoviesDatastore.searchMovies(query) }
        confirmVerified(remoteMoviesDatastore, localMoviesDatastore)
    }
}
