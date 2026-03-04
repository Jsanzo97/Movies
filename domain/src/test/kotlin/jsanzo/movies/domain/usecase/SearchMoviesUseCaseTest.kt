package jsanzo.movies.domain.usecase

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import jsanzo.movies.domain.error.InvalidParametersError
import jsanzo.movies.domain.model.DomainMovie
import jsanzo.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class SearchMoviesUseCaseTest {

    private val repository: MoviesRepository = mockk()
    private val useCase = SearchMoviesUseCase(repository)

    private val query = "harry potter"

    @Test
    fun `invoke calls repository searchMovies with query`() = runTest {
        val domainMovie = mockk<DomainMovie>()
        coEvery { repository.searchMovies(query) } returns domainMovie.right()

        val result = useCase(query)

        coVerify(exactly = 1) { repository.searchMovies(query) }
        result shouldBe domainMovie.right()
    }

    @Test
    fun `invoke returns error when repository fails`() = runTest {
        coEvery { repository.searchMovies(query) } returns InvalidParametersError.left()

        val result = useCase(query)

        result shouldBe InvalidParametersError.left()
    }
}
