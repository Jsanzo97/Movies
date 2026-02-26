package service.movies

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import jsanzo.movies.data.error.UnrecognizedRemoteError
import jsanzo.movies.remote.dto.response.toDataMovie
import jsanzo.movies.remote.dto.response.toDataMovieDetails
import jsanzo.movies.remote.service.movies.MoviesRemoteWebService
import jsanzo.movies.remote.service.movies.MoviesService
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import retrofit2.Response
import service.model.getMovieDetails
import service.model.getMovieResponse
import java.io.IOException

class MoviesServiceTest {

    private val moviesRemoteWebService: MoviesRemoteWebService = mockk()
    private val apiKey = "12345"
    private val moviesService = MoviesService(moviesRemoteWebService, apiKey)

    private val page = 1
    private val movieId = 123

    @Test
    fun `getMovies returns data movie on success`() = runTest {
        val response = Response.success(getMovieResponse)
        coEvery { moviesRemoteWebService.getMovies(page, apiKey) } returns response

        val result = moviesService.getMovies(page)

        result shouldBe getMovieResponse.toDataMovie().right()
    }

    @Test
    fun `getMovies returns movie error on failure`() = runTest {
        coEvery { moviesRemoteWebService.getMovies(page, apiKey) } throws IOException("Network Error")

        val result = moviesService.getMovies(page)

        result shouldBe UnrecognizedRemoteError("Network Error").left()
    }

    @Test
    fun `getMovieDetails returns domain movie details on success`() = runTest {
        val response = Response.success(getMovieDetails)
        coEvery { moviesRemoteWebService.getMovieDetails(movieId, apiKey) } returns response

        val result = moviesService.getMovieDetails(movieId)

        result shouldBe getMovieDetails.toDataMovieDetails().right()
    }

    @Test
    fun `getMovieDetails without collection returns domain movie details without collection on success`() = runTest {
        val response = Response.success(getMovieDetails.copy(belongsToCollection = null))
        coEvery { moviesRemoteWebService.getMovieDetails(movieId, apiKey) } returns response

        val result = moviesService.getMovieDetails(movieId)

        result shouldBe getMovieDetails.toDataMovieDetails().copy(belongsToCollection = null).right()
    }

    @Test
    fun `getMovieDetails returns movie error on failure`() = runTest {
        coEvery { moviesRemoteWebService.getMovieDetails(movieId, apiKey) } throws IOException("Network Error")

        val result = moviesService.getMovieDetails(movieId)

        result shouldBe UnrecognizedRemoteError("Network Error").left()
    }
}
