package jsanzo.movies.domain.usecase

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import jsanzo.movies.domain.error.NotFoundError
import jsanzo.movies.domain.model.domainMovie
import jsanzo.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetMoviesUseCaseTest {

    private val repository: MoviesRepository = mockk()
    private val useCase = GetMoviesUseCase(repository)

    private val page = 1

    @Test
    fun `Given a valid page, When invoke is called, Then movies are returned`() = runTest {
        coEvery { repository.getMovies(page) } returns listOf(domainMovie).right()

        val result = useCase(page)

        result shouldBe listOf(domainMovie).right()
        coVerify(exactly = 1) { repository.getMovies(page) }
        confirmVerified(repository)
    }

    @Test
    fun `Given a repository error, When invoke is called, Then error is returned`() = runTest {
        coEvery { repository.getMovies(page) } returns NotFoundError.left()

        val result = useCase(page)

        result shouldBe NotFoundError.left()
        coVerify(exactly = 1) { repository.getMovies(page) }
        confirmVerified(repository)
    }
}
