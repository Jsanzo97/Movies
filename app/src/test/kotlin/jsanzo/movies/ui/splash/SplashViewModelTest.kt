package jsanzo.movies.ui.splash

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import jsanzo.movies.domain.error.MovieError
import jsanzo.movies.domain.usecase.MustUpdateUseCase
import jsanzo.movies.tracking.MovieTracker
import jsanzo.movies.ui.screens.splash.MustUpdate
import jsanzo.movies.ui.screens.splash.SplashLoading
import jsanzo.movies.ui.screens.splash.SplashViewModel
import jsanzo.movies.ui.screens.splash.UpToDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mustUpdateUseCase: MustUpdateUseCase = mockk()
    private val tracker: MovieTracker = mockk(relaxed = true)
    private lateinit var viewModel: SplashViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SplashViewModel(mustUpdateUseCase, tracker)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is SplashLoading`() {
        viewModel.state.value shouldBe SplashLoading
    }

    @Test
    fun `trackScreenView calls tracker trackSplashShown`() {
        viewModel.trackScreenView()
        verify { tracker.trackSplashShown() }
    }

    @Test
    fun `mustUpdate transitions to MustUpdate when use case returns true`() = runTest {
        coEvery { mustUpdateUseCase("1.0.0") } returns true.right()

        viewModel.mustUpdate("1.0.0")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.value shouldBe MustUpdate
    }

    @Test
    fun `mustUpdate calls trackForceUpdateShown when use case returns true`() = runTest {
        coEvery { mustUpdateUseCase("1.0.0") } returns true.right()

        viewModel.mustUpdate("1.0.0")
        testDispatcher.scheduler.advanceUntilIdle()

        verify { tracker.trackForceUpdateShown("1.0.0") }
    }

    @Test
    fun `mustUpdate transitions to UpToDate when use case returns false`() = runTest {
        coEvery { mustUpdateUseCase("2.0.0") } returns false.right()

        viewModel.mustUpdate("2.0.0")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.value shouldBe UpToDate
    }

    @Test
    fun `mustUpdate transitions to UpToDate when use case returns error`() = runTest {
        val error = mockk<MovieError>()
        coEvery { mustUpdateUseCase("1.0.0") } returns error.left()

        viewModel.mustUpdate("1.0.0")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.value shouldBe UpToDate
    }

    @Test
    fun `mustUpdate calls trackRemoteConfigError when use case returns error`() = runTest {
        val error = mockk<MovieError>()
        coEvery { mustUpdateUseCase("1.0.0") } returns error.left()

        viewModel.mustUpdate("1.0.0")
        testDispatcher.scheduler.advanceUntilIdle()

        verify { tracker.trackRemoteConfigError() }
    }

    @Test
    fun `mustUpdate error message is accessible`() {
        SplashLoading.toString() shouldBe "SplashLoading"
        MustUpdate.toString() shouldBe "MustUpdate"
        UpToDate.toString() shouldBe "UpToDate"
    }
}
