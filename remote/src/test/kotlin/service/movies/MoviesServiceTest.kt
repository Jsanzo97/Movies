package service.movies

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import jsanzo.movies.data.error.UnrecognizedRemoteError
import jsanzo.movies.remote.dto.response.GetMoviesDetailsResponse
import jsanzo.movies.remote.dto.response.GetMoviesResponse
import jsanzo.movies.remote.dto.response.toDataMovie
import jsanzo.movies.remote.dto.response.toDataMovieDetails
import jsanzo.movies.remote.service.NetworkHandler
import jsanzo.movies.remote.service.movies.MoviesRemoteWebService
import jsanzo.movies.remote.service.movies.MoviesService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import retrofit2.Response
import service.model.getMovieDetails
import service.model.getMovieResponse

class MoviesServiceTest {

    private val moviesRemoteWebService: MoviesRemoteWebService = mockk()
    private val networkHandler: NetworkHandler = mockk()
    private val apiKey = "12345"
    private val moviesService = MoviesService(moviesRemoteWebService, networkHandler, apiKey)

    private val page = 1
    private val movieId = 123
    private val networkError = UnrecognizedRemoteError("Network Error")

    @Test
    fun `Given a valid page, When getMovies is called, Then data movie is returned`() = runTest {
        coEvery { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesResponse>>()) } returns getMovieResponse.right()

        val result = moviesService.getMovies(page)

        result shouldBe getMovieResponse.results.toDataMovie().right()
        coVerify(exactly = 1) { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesResponse>>()) }
        confirmVerified(networkHandler)
    }

    @Test
    fun `Given a network error, When getMovies is called, Then error is returned`() = runTest {
        coEvery { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesResponse>>()) } returns networkError.left()

        val result = moviesService.getMovies(page)

        result shouldBe networkError.left()
        coVerify(exactly = 1) { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesResponse>>()) }
        confirmVerified(networkHandler)
    }

    @Test
    fun `Given a valid movie id, When getMovieDetails is called, Then data movie details are returned`() = runTest {
        coEvery { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesDetailsResponse>>()) } returns getMovieDetails.right()

        val result = moviesService.getMovieDetails(movieId)

        result shouldBe getMovieDetails.toDataMovieDetails().right()
        coVerify(exactly = 1) { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesDetailsResponse>>()) }
        confirmVerified(networkHandler)
    }

    @Test
    fun `Given movie details without collection, When getMovieDetails is called, Then data movie details without collection are returned`() = runTest {
        val detailsWithoutCollection = getMovieDetails.copy(belongsToCollection = null)
        coEvery { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesDetailsResponse>>()) } returns detailsWithoutCollection.right()

        val result = moviesService.getMovieDetails(movieId)

        result shouldBe detailsWithoutCollection.toDataMovieDetails().right()
        coVerify(exactly = 1) { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesDetailsResponse>>()) }
        confirmVerified(networkHandler)
    }

    @Test
    fun `Given a network error, When getMovieDetails is called, Then error is returned`() = runTest {
        coEvery { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesDetailsResponse>>()) } returns networkError.left()

        val result = moviesService.getMovieDetails(movieId)

        result shouldBe networkError.left()
        coVerify(exactly = 1) { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesDetailsResponse>>()) }
        confirmVerified(networkHandler)
    }

    @Test
    fun `Given a query, When searchMovies is called, Then data movie is returned`() = runTest {
        coEvery { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesResponse>>()) } returns getMovieResponse.right()

        val result = moviesService.searchMovies("harry potter")

        result shouldBe getMovieResponse.results.toDataMovie().right()
        coVerify(exactly = 1) { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesResponse>>()) }
        confirmVerified(networkHandler)
    }

    @Test
    fun `Given a network error, When searchMovies is called, Then error is returned`() = runTest {
        coEvery { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesResponse>>()) } returns networkError.left()

        val result = moviesService.searchMovies("harry potter")

        result shouldBe networkError.left()
        coVerify(exactly = 1) { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesResponse>>()) }
        confirmVerified(networkHandler)
    }
}
