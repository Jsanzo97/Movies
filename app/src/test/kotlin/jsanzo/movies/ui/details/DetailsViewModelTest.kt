package jsanzo.movies.ui.details

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import jsanzo.movies.domain.error.NotFoundError
import jsanzo.movies.domain.usecase.GetMovieDetailsUseCase
import jsanzo.movies.tracking.MovieTracker
import jsanzo.movies.ui.model.domainMovieDetails
import jsanzo.movies.ui.screens.details.DetailsError
import jsanzo.movies.ui.screens.details.DetailsSuccess
import jsanzo.movies.ui.screens.details.DetailsViewModel
import jsanzo.movies.ui.screens.details.DetailsViewState
import jsanzo.movies.ui.screens.details.Loading
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class DetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var detailsViewModel: DetailsViewModel

    private val mockedGetMovieDetailsUseCase: GetMovieDetailsUseCase = mockk()

    private lateinit var detailsViewModelStateFlow: StateFlow<DetailsViewState>

    private val validMovieId = 0
    private val invalidMovieId = -1

    private val mockedMovieTracker: MovieTracker = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockedGetMovieDetailsUseCase(invalidMovieId) } returns NotFoundError.left()
        coEvery { mockedGetMovieDetailsUseCase(validMovieId) } returns domainMovieDetails.right()

        detailsViewModel = DetailsViewModel(mockedGetMovieDetailsUseCase, mockedMovieTracker)
        detailsViewModelStateFlow = detailsViewModel.state
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `we are always in Loading state at the beginning`() {
        detailsViewModelStateFlow.value.shouldBeInstanceOf<Loading>()
    }

    @Test
    fun `we are in DetailsSuccess state after call getDetails() with valid id, also we get the movie details`() = runTest {
        detailsViewModel.getDetails(validMovieId)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedGetMovieDetailsUseCase(validMovieId) }

        detailsViewModelStateFlow.value.shouldBeInstanceOf<DetailsSuccess>()

        val state = detailsViewModelStateFlow.value as? DetailsSuccess

        state?.movieDetails shouldBe domainMovieDetails
    }

    @Test
    fun `we are in DetailsError state after call getDetails() with invalid id`() = runTest {
        detailsViewModel.getDetails(invalidMovieId)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedGetMovieDetailsUseCase(invalidMovieId) }

        detailsViewModelStateFlow.value.shouldBeInstanceOf<DetailsError>()
    }

    @Test
    fun `trackScreenView calls tracker trackDetailsShown`() = runTest {
        detailsViewModel.trackScreenView(validMovieId)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedMovieTracker.trackDetailsShown(validMovieId) }
    }

    @Test
    fun `DetailsError contains correct message`() {
        val error = DetailsError("An unexpected error occurred")
        error.message shouldBe "An unexpected error occurred"
    }
}
