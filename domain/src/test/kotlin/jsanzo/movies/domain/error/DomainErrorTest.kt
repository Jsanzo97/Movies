package jsanzo.movies.domain.error

import arrow.core.left
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import jsanzo.movies.domain.repository.MoviesRepository
import jsanzo.movies.domain.usecase.GetMoviesUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DomainErrorTest {

    private val repository: MoviesRepository = mockk()
    private val useCase = GetMoviesUseCase(repository)

    private val page = 1

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
    fun `invoke returns NotFoundError when repository fails with not found error`() = runTest {
        coEvery { repository.getMovies(page) } returns NotFoundError.left()

        val result = useCase(page)

        result shouldBe NotFoundError.left()
    }

    @Test
    fun `invoke returns UnknownIOError when repository fails with unknown error`() = runTest {
        coEvery { repository.getMovies(page) } returns UnknownIOError.left()

        val result = useCase(page)

        result shouldBe UnknownIOError.left()
    }

    @Test
    fun `invoke returns IOOperationError when repository fails with io error error`() = runTest {
        coEvery { repository.getMovies(page) } returns IOOperationError.left()

        val result = useCase(page)

        result shouldBe IOOperationError.left()
    }

    @Test
    fun `invoke returns GenericError when repository fails with generic error`() = runTest {
        val error = GenericError("something went wrong")
        coEvery { repository.getMovies(page) } returns error.left()

        val result = useCase(page)

        result shouldBe error.left()
    }
}
