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
import jsanzo.movies.domain.model.domainMovie
import jsanzo.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetMoviesUseCaseTest {

    private val repository: MoviesRepository = mockk()
    private val useCase = GetMoviesUseCase(repository)

    private val page = 1

    @Test
    fun `invoke returns movies when repository succeeds`() = runTest {
        coEvery { repository.getMovies(page) } returns domainMovie.right()

        val result = useCase(page)

        coVerify(exactly = 1) { repository.getMovies(page) }
        result shouldBe domainMovie.right()
    }

    @Test
    fun `invoke returns error when repository fails`() = runTest {
        coEvery { repository.getMovies(page) } returns NotFoundError.left()

        val result = useCase(page)

        coVerify(exactly = 1) { repository.getMovies(page) }
        result shouldBe NotFoundError.left()
    }

    @Test
    fun `invoke returns InvalidParametersError when repository fails with invalid params`() = runTest {
        coEvery { repository.getMovies(page) } returns InvalidParametersError.left()

        val result = useCase(page)

        result shouldBe InvalidParametersError.left()
    }

    @Test
    fun `invoke returns AuthenticationError when repository fails with auth error`() = runTest {
        coEvery { repository.getMovies(page) } returns AuthenticationError.left()

        val result = useCase(page)

        result shouldBe AuthenticationError.left()
    }

    @Test
    fun `invoke returns UnknownIOError when repository fails with unknown error`() = runTest {
        coEvery { repository.getMovies(page) } returns UnknownIOError.left()

        val result = useCase(page)

        result shouldBe UnknownIOError.left()
    }

    @Test
    fun `invoke returns GenericError when repository fails with generic error`() = runTest {
        val error = GenericError("something went wrong")
        coEvery { repository.getMovies(page) } returns error.left()

        val result = useCase(page)

        result shouldBe error.left()
        result.shouldBeInstanceOf<Either.Left<GenericError>>()
        result.value.message shouldBe "something went wrong"
    }

    @Test
    fun `invoke returns IOOperation when repository fails with io operation error`() = runTest {
        coEvery { repository.getMovies(page) } returns IOOperationError.left()

        val result = useCase(page)

        result shouldBe IOOperationError.left()
    }
}
