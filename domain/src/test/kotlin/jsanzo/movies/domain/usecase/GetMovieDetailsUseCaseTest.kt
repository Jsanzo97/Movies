package jsanzo.movies.domain.usecase

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import jsanzo.movies.domain.error.NotFoundError
import jsanzo.movies.domain.model.domainMovieDetails
import jsanzo.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class GetMovieDetailsUseCaseTest {

    private val repository: MoviesRepository = mockk()
    private val useCase = GetMovieDetailsUseCase(repository)

    private val movieId = 1

    @Test
    fun `Given a valid movie id, When invoke is called, Then movie details are returned`() = runTest {
        coEvery { repository.getMovieDetails(movieId) } returns domainMovieDetails.right()

        val result = useCase(movieId)

        result shouldBe domainMovieDetails.right()
        coVerify(exactly = 1) { repository.getMovieDetails(movieId) }
        confirmVerified(repository)
    }

    @Test
    fun `Given a repository error, When invoke is called, Then error is returned`() = runTest {
        coEvery { repository.getMovieDetails(movieId) } returns NotFoundError.left()

        val result = useCase(movieId)

        result shouldBe NotFoundError.left()
        coVerify(exactly = 1) { repository.getMovieDetails(movieId) }
        confirmVerified(repository)
    }
}
