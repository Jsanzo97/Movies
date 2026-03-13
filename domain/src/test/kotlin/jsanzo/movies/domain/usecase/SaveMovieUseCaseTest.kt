package jsanzo.movies.domain.usecase

import arrow.core.None
import arrow.core.some
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import jsanzo.movies.domain.error.IOOperationError
import jsanzo.movies.domain.model.domainMovie
import jsanzo.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SaveMovieUseCaseTest {

    private val repository: MoviesRepository = mockk()
    private val useCase = SaveMovieUseCase(repository)

    @Test
    fun `Given a valid movie, When invoke is called, Then None is returned`() = runTest {
        coEvery { repository.saveMovie(domainMovie) } returns None

        val result = useCase(domainMovie)

        result shouldBe None
        coVerify(exactly = 1) { repository.saveMovie(domainMovie) }
        confirmVerified(repository)
    }

    @Test
    fun `Given a repository error, When invoke is called, Then error is returned`() = runTest {
        coEvery { repository.saveMovie(domainMovie) } returns IOOperationError.some()

        val result = useCase(domainMovie)

        result shouldBe IOOperationError.some()
        coVerify(exactly = 1) { repository.saveMovie(domainMovie) }
        confirmVerified(repository)
    }
}
