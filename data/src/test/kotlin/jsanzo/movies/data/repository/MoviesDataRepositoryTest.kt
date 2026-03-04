package jsanzo.movies.data.repository

import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import arrow.core.left
import arrow.core.right
import arrow.core.some
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import jsanzo.movies.data.datastore.LocalMoviesDatastore
import jsanzo.movies.data.datastore.RemoteMoviesDatastore
import jsanzo.movies.data.error.NotFound
import jsanzo.movies.data.error.ReadingError
import jsanzo.movies.data.error.UnknownError
import jsanzo.movies.data.error.WritingError
import jsanzo.movies.data.model.dataMovie
import jsanzo.movies.data.model.dataMovieDetails
import jsanzo.movies.data.model.domainMovieResult
import jsanzo.movies.data.model.toDataMovieResult
import jsanzo.movies.data.model.toMovie
import jsanzo.movies.data.model.toMovieDetails
import jsanzo.movies.domain.error.IOOperationError
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
        repository =
            MoviesDataRepository(remoteMoviesDatastore, localMoviesDatastore, testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getMovies returns remote data when remote call succeeds`() = runTest {
        coEvery { remoteMoviesDatastore.getMovies(validPage) } returns dataMovie.right()

        val result = repository.getMovies(validPage)

        coVerify(exactly = 1) { remoteMoviesDatastore.getMovies(validPage) }
        coVerify(exactly = 0) { localMoviesDatastore.getMovies() }
        result shouldBe dataMovie.toMovie().right()
    }

    @Test
    fun `getMovies returns local data when remote call fails`() = runTest {
        coEvery { remoteMoviesDatastore.getMovies(validPage) } returns NotFound.left()
        coEvery { localMoviesDatastore.getMovies() } returns dataMovie.right()

        val result = repository.getMovies(validPage)

        coVerify(exactly = 1) { remoteMoviesDatastore.getMovies(validPage) }
        coVerify(exactly = 1) { localMoviesDatastore.getMovies() }
        result shouldBe dataMovie.toMovie().right()
    }

    @Test
    fun `getMovies returns local data when remote fails with unknown error`() = runTest {
        coEvery { remoteMoviesDatastore.getMovies(validPage) } returns UnknownError.left()
        coEvery { localMoviesDatastore.getMovies() } returns dataMovie.right()

        val result = repository.getMovies(validPage)

        result shouldBe dataMovie.toMovie().right()
    }

    @Test
    fun `getMovies returns error when both remote and local fail`() = runTest {
        coEvery { remoteMoviesDatastore.getMovies(validPage) } returns NotFound.left()
        coEvery { localMoviesDatastore.getMovies() } returns ReadingError.left()

        val result = repository.getMovies(validPage)

        result.shouldBeInstanceOf<Either.Left<*>>()
        result.value shouldBe IOOperationError
    }

    @Test
    fun `getMovies with invalid page returns error from remote`() = runTest {
        coEvery { remoteMoviesDatastore.getMovies(invalidPage) } returns NotFound.left()
        coEvery { localMoviesDatastore.getMovies() } returns ReadingError.left()

        val result = repository.getMovies(invalidPage)

        result.shouldBeInstanceOf<Either.Left<*>>()
        result.value shouldBe IOOperationError
    }

    @Test
    fun `getMovieDetails returns remote data and saves locally when remote call succeeds`() = runTest {
        coEvery { remoteMoviesDatastore.getMovieDetails(movieId) } returns dataMovieDetails.right()
        coEvery { localMoviesDatastore.saveMovieDetails(dataMovieDetails) } returns None

        val result = repository.getMovieDetails(movieId)

        coVerify(exactly = 1) { remoteMoviesDatastore.getMovieDetails(movieId) }
        coVerify(exactly = 1) { localMoviesDatastore.saveMovieDetails(dataMovieDetails) }
        coVerify(exactly = 0) { localMoviesDatastore.getMovieDetails(any()) }
        result shouldBe dataMovieDetails.toMovieDetails().right()
    }

    @Test
    fun `getMovieDetails returns error when remote succeeds but local save fails`() = runTest {
        coEvery { remoteMoviesDatastore.getMovieDetails(movieId) } returns dataMovieDetails.right()
        coEvery { localMoviesDatastore.saveMovieDetails(dataMovieDetails) } returns WritingError.some()
        coEvery { localMoviesDatastore.getMovieDetails(movieId) } returns ReadingError.left()

        val result = repository.getMovieDetails(movieId)

        result.shouldBeInstanceOf<Either.Left<*>>()
        result.value shouldBe IOOperationError
    }

    @Test
    fun `getMovieDetails returns local data when remote call fails`() = runTest {
        coEvery { remoteMoviesDatastore.getMovieDetails(movieId) } returns NotFound.left()
        coEvery { localMoviesDatastore.getMovieDetails(movieId) } returns dataMovieDetails.right()

        val result = repository.getMovieDetails(movieId)

        coVerify(exactly = 1) { remoteMoviesDatastore.getMovieDetails(movieId) }
        coVerify(exactly = 1) { localMoviesDatastore.getMovieDetails(movieId) }
        result shouldBe dataMovieDetails.toMovieDetails().right()
    }

    @Test
    fun `getMovieDetails falls back to local when remote fails with unknown error`() = runTest {
        coEvery { remoteMoviesDatastore.getMovieDetails(movieId) } returns UnknownError.left()
        coEvery { localMoviesDatastore.getMovieDetails(movieId) } returns dataMovieDetails.right()

        val result = repository.getMovieDetails(movieId)

        result shouldBe dataMovieDetails.toMovieDetails().right()
    }

    @Test
    fun `getMovieDetails returns error when both remote and local fail`() = runTest {
        coEvery { remoteMoviesDatastore.getMovieDetails(movieId) } returns NotFound.left()
        coEvery { localMoviesDatastore.getMovieDetails(movieId) } returns ReadingError.left()

        val result = repository.getMovieDetails(movieId)

        result.shouldBeInstanceOf<Either.Left<*>>()
        result.value shouldBe IOOperationError
    }

    @Test
    fun `saveMovie returns None when local save succeeds`() = runTest {
        coEvery { localMoviesDatastore.saveMovie(domainMovieResult.toDataMovieResult()) } returns None

        val result = repository.saveMovie(domainMovieResult)

        coVerify(exactly = 1) { localMoviesDatastore.saveMovie(domainMovieResult.toDataMovieResult()) }
        result shouldBe None
    }

    @Test
    fun `saveMovie returns error when local save fails`() = runTest {
        coEvery { localMoviesDatastore.saveMovie(domainMovieResult.toDataMovieResult()) } returns WritingError.some()

        val result = repository.saveMovie(domainMovieResult)

        result.shouldBeInstanceOf<Some<*>>()
        result.getOrNull() shouldBe IOOperationError
    }

    @Test
    fun `searchMovies returns remote data when remote call succeeds`() = runTest {
        val query = "harry potter"
        coEvery { remoteMoviesDatastore.searchMovies(query) } returns dataMovie.right()

        val result = repository.searchMovies(query)

        coVerify(exactly = 1) { remoteMoviesDatastore.searchMovies(query) }
        coVerify(exactly = 0) { localMoviesDatastore.searchMovies(any()) }
        result shouldBe dataMovie.toMovie().right()
    }

    @Test
    fun `searchMovies returns local data when remote call fails`() = runTest {
        val query = "harry potter"
        coEvery { remoteMoviesDatastore.searchMovies(query) } returns NotFound.left()
        coEvery { localMoviesDatastore.searchMovies(query) } returns dataMovie.right()

        val result = repository.searchMovies(query)

        coVerify(exactly = 1) { remoteMoviesDatastore.searchMovies(query) }
        coVerify(exactly = 1) { localMoviesDatastore.searchMovies(query) }
        result shouldBe dataMovie.toMovie().right()
    }

    @Test
    fun `searchMovies returns error when both remote and local fail`() = runTest {
        val query = "harry potter"
        coEvery { remoteMoviesDatastore.searchMovies(query) } returns NotFound.left()
        coEvery { localMoviesDatastore.searchMovies(query) } returns ReadingError.left()

        val result = repository.searchMovies(query)

        result.shouldBeInstanceOf<Either.Left<*>>()
        result.value shouldBe IOOperationError
    }
}
