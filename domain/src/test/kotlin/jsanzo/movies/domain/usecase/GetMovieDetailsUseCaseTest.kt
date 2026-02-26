package jsanzo.movies.domain.usecase

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import jsanzo.movies.domain.error.AuthenticationError
import jsanzo.movies.domain.error.GenericError
import jsanzo.movies.domain.error.IOOperationError
import jsanzo.movies.domain.error.InvalidParametersError
import jsanzo.movies.domain.error.NotFoundError
import jsanzo.movies.domain.error.UnknownIOError
import jsanzo.movies.domain.model.domainMovieDetails
import jsanzo.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetMovieDetailsUseCaseTest {

    private val repository: MoviesRepository = mockk()
    private val useCase = GetMovieDetailsUseCase(repository)

    private val movieId = 1

    @Test
    fun `invoke returns movie details when repository succeeds`() = runTest {
        coEvery { repository.getMovieDetails(movieId) } returns domainMovieDetails.right()

        val result = useCase(movieId)

        coVerify(exactly = 1) { repository.getMovieDetails(movieId) }
        result shouldBe domainMovieDetails.right()
    }

    @Test
    fun `invoke returns error when repository fails`() = runTest {
        coEvery { repository.getMovieDetails(movieId) } returns NotFoundError.left()

        val result = useCase(movieId)

        coVerify(exactly = 1) { repository.getMovieDetails(movieId) }
        result shouldBe NotFoundError.left()
    }

    @Test
    fun `invoke returns InvalidParametersError when repository fails with invalid params`() = runTest {
        coEvery { repository.getMovieDetails(movieId) } returns InvalidParametersError.left()

        val result = useCase(movieId)

        result shouldBe InvalidParametersError.left()
    }

    @Test
    fun `invoke returns AuthenticationError when repository fails with auth error`() = runTest {
        coEvery { repository.getMovieDetails(movieId) } returns AuthenticationError.left()

        val result = useCase(movieId)

        result shouldBe AuthenticationError.left()
    }

    @Test
    fun `invoke returns UnknownIOError when repository fails with unknown error`() = runTest {
        coEvery { repository.getMovieDetails(movieId) } returns UnknownIOError.left()

        val result = useCase(movieId)

        result shouldBe UnknownIOError.left()
    }

    @Test
    fun `invoke returns GenericError when repository fails with generic error`() = runTest {
        val error = GenericError("something went wrong")
        coEvery { repository.getMovieDetails(movieId) } returns error.left()

        val result = useCase(movieId)

        result shouldBe error.left()
        result.shouldBeInstanceOf<Either.Left<GenericError>>()
        result.value.message shouldBe "something went wrong"
    }

    @Test
    fun `invoke returns IOOperation when repository fails with io operation error`() = runTest {
        coEvery { repository.getMovieDetails(movieId) } returns IOOperationError.left()

        val result = useCase(movieId)

        result shouldBe IOOperationError.left()
    }
}
