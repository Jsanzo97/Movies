package jsanzo.movies.domain.usecase

import arrow.core.None
import arrow.core.Some
import arrow.core.some
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
import jsanzo.movies.domain.model.domainMovieResult
import jsanzo.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SaveMovieUseCaseTest {

    private val repository: MoviesRepository = mockk()
    private val useCase = SaveMovieUseCase(repository)

    @Test
    fun `invoke returns None when save succeeds`() = runTest {
        coEvery { repository.saveMovie(domainMovieResult) } returns None

        val result = useCase(domainMovieResult)

        coVerify(exactly = 1) { repository.saveMovie(domainMovieResult) }
        result shouldBe None
    }

    @Test
    fun `invoke returns error when save fails`() = runTest {
        coEvery { repository.saveMovie(domainMovieResult) } returns IOOperationError.some()

        val result = useCase(domainMovieResult)

        coVerify(exactly = 1) { repository.saveMovie(domainMovieResult) }
        result shouldBe IOOperationError.some()
    }

    @Test
    fun `invoke returns error when repository fails`() = runTest {
        coEvery { repository.saveMovie(domainMovieResult) } returns NotFoundError.some()

        val result = useCase(domainMovieResult)

        coVerify(exactly = 1) { repository.saveMovie(domainMovieResult) }
        result shouldBe NotFoundError.some()
    }

    @Test
    fun `invoke returns InvalidParametersError when repository fails with invalid params`() = runTest {
        coEvery { repository.saveMovie(domainMovieResult) } returns InvalidParametersError.some()

        val result = useCase(domainMovieResult)

        result shouldBe InvalidParametersError.some()
    }

    @Test
    fun `invoke returns AuthenticationError when repository fails with auth error`() = runTest {
        coEvery { repository.saveMovie(domainMovieResult) } returns AuthenticationError.some()

        val result = useCase(domainMovieResult)

        result shouldBe AuthenticationError.some()
    }

    @Test
    fun `invoke returns UnknownIOError when repository fails with unknown error`() = runTest {
        coEvery { repository.saveMovie(domainMovieResult) } returns UnknownIOError.some()

        val result = useCase(domainMovieResult)

        result shouldBe UnknownIOError.some()
    }

    @Test
    fun `invoke returns GenericError when repository fails with generic error`() = runTest {
        val error = GenericError("something went wrong")
        coEvery { repository.saveMovie(domainMovieResult) } returns error.some()

        val result = useCase(domainMovieResult)

        result shouldBe error.some()
        result.shouldBeInstanceOf<Some<GenericError>>()
        result.value.message shouldBe "something went wrong"
    }

    @Test
    fun `invoke returns IOOperation when repository fails with io operation error`() = runTest {
        coEvery { repository.saveMovie(domainMovieResult) } returns IOOperationError.some()

        val result = useCase(domainMovieResult)

        result shouldBe IOOperationError.some()
    }
}
