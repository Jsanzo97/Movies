package service.movies

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
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

    @Test
    fun `getMovies returns data movie on success`() = runTest {
        coEvery { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesResponse>>()) } returns getMovieResponse.right()

        val result = moviesService.getMovies(page)

        result shouldBe getMovieResponse.toDataMovie().right()
    }

    @Test
    fun `getMovies returns movie error on failure`() = runTest {
        coEvery { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesResponse>>()) } returns UnrecognizedRemoteError("Network Error").left()

        val result = moviesService.getMovies(page)

        result shouldBe UnrecognizedRemoteError("Network Error").left()
    }

    @Test
    fun `getMovieDetails returns domain movie details on success`() = runTest {
        coEvery { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesDetailsResponse>>()) } returns getMovieDetails.right()

        val result = moviesService.getMovieDetails(movieId)

        result shouldBe getMovieDetails.toDataMovieDetails().right()
    }

    @Test
    fun `getMovieDetails without collection returns domain movie details without collection on success`() = runTest {
        val detailsWithoutCollection = getMovieDetails.copy(belongsToCollection = null)
        coEvery { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesDetailsResponse>>()) } returns detailsWithoutCollection.right()

        val result = moviesService.getMovieDetails(movieId)

        result shouldBe detailsWithoutCollection.toDataMovieDetails().right()
    }

    @Test
    fun `getMovieDetails returns movie error on failure`() = runTest {
        coEvery { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesDetailsResponse>>()) } returns UnrecognizedRemoteError("Network Error").left()

        val result = moviesService.getMovieDetails(movieId)

        result shouldBe UnrecognizedRemoteError("Network Error").left()
    }

    @Test
    fun `searchMovies returns data movie on success`() = runTest {
        coEvery { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesResponse>>()) } returns getMovieResponse.right()

        val result = moviesService.searchMovies("harry potter")

        result shouldBe getMovieResponse.toDataMovie().right()
    }

    @Test
    fun `searchMovies returns movie error on failure`() = runTest {
        coEvery { networkHandler.executeNetworkRequest(any<suspend () -> Response<GetMoviesResponse>>()) } returns UnrecognizedRemoteError("Network Error").left()

        val result = moviesService.searchMovies("harry potter")

        result shouldBe UnrecognizedRemoteError("Network Error").left()
    }
}
