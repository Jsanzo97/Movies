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

class SearchMoviesUseCaseTest {

    private val repository: MoviesRepository = mockk()
    private val useCase = SearchMoviesUseCase(repository)

    private val query = "harry potter"

    @Test
    fun `Given a query, When invoke is called, Then movies are returned`() = runTest {
        coEvery { repository.searchMovies(query) } returns listOf(domainMovie).right()

        val result = useCase(query)

        result shouldBe listOf(domainMovie).right()
        coVerify(exactly = 1) { repository.searchMovies(query) }
        confirmVerified(repository)
    }

    @Test
    fun `Given a repository error, When invoke is called, Then error is returned`() = runTest {
        coEvery { repository.searchMovies(query) } returns NotFoundError.left()

        val result = useCase(query)

        result shouldBe NotFoundError.left()
        coVerify(exactly = 1) { repository.searchMovies(query) }
        confirmVerified(repository)
    }
}
