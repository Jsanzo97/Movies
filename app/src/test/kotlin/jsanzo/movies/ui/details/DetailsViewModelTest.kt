package jsanzo.movies.ui.details

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import jsanzo.movies.domain.error.NotFoundError
import jsanzo.movies.domain.usecase.GetMovieDetailsUseCase
import jsanzo.movies.presentation.DetailsViewModel
import jsanzo.movies.tracking.MovieTracker
import jsanzo.movies.ui.model.domainMovieDetails
import jsanzo.movies.ui.screens.details.DetailsError
import jsanzo.movies.ui.screens.details.DetailsSuccess
import jsanzo.movies.ui.screens.details.Loading
import jsanzo.movies.ui.screens.details.toMovieDetailsUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val mockedMovieTracker: MovieTracker = mockk(relaxed = true)

    private val validMovieId = 1
    private val invalidMovieId = -1

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        detailsViewModel = DetailsViewModel(mockedGetMovieDetailsUseCase, mockedMovieTracker)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given valid movie id, When getDetails is called, Then state is DetailsSuccess`() = runTest {
        coEvery { mockedGetMovieDetailsUseCase(validMovieId) } returns domainMovieDetails.right()

        detailsViewModel.state.test {
            awaitItem() shouldBe Loading

            detailsViewModel.getDetails(validMovieId)

            awaitItem() shouldBe DetailsSuccess(domainMovieDetails.toMovieDetailsUi())

            coVerify(exactly = 1) { mockedGetMovieDetailsUseCase(validMovieId) }
            confirmVerified(mockedGetMovieDetailsUseCase)
        }
    }

    @Test
    fun `Given invalid movie id, When getDetails is called, Then state is DetailsError`() = runTest {
        coEvery { mockedGetMovieDetailsUseCase(invalidMovieId) } returns NotFoundError.left()

        detailsViewModel.state.test {
            awaitItem() shouldBe Loading

            detailsViewModel.getDetails(invalidMovieId)

            awaitItem() shouldBe DetailsError(NotFoundError.toString())

            coVerify(exactly = 1) { mockedGetMovieDetailsUseCase(invalidMovieId) }
            confirmVerified(mockedGetMovieDetailsUseCase)
        }
    }

    @Test
    fun `Given invalid movie id, When getDetails is called, Then trackErrorShown is called`() = runTest {
        coEvery { mockedGetMovieDetailsUseCase(invalidMovieId) } returns NotFoundError.left()

        detailsViewModel.state.test {
            awaitItem() shouldBe Loading

            detailsViewModel.getDetails(invalidMovieId)

            awaitItem() shouldBe DetailsError(NotFoundError.toString())

            coVerify(exactly = 1) { mockedGetMovieDetailsUseCase(invalidMovieId) }
            verify(exactly = 1) { mockedMovieTracker.trackErrorShown("details", NotFoundError.toString()) }
            confirmVerified(mockedGetMovieDetailsUseCase, mockedMovieTracker)
        }
    }

    @Test
    fun `Given any movie id, When trackScreenView is called, Then trackDetailsShown is called`() = runTest {
        detailsViewModel.state.test {
            awaitItem() shouldBe Loading

            detailsViewModel.trackScreenView(validMovieId)

            coVerify(exactly = 1) { mockedMovieTracker.trackDetailsShown(validMovieId) }
            confirmVerified(mockedMovieTracker)
        }
    }
}
