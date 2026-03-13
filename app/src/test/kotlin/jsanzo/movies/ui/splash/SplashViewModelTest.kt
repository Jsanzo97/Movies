package jsanzo.movies.ui.splash

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import jsanzo.movies.domain.error.MovieError
import jsanzo.movies.domain.usecase.MustUpdateUseCase
import jsanzo.movies.presentation.SplashViewModel
import jsanzo.movies.tracking.MovieTracker
import jsanzo.movies.ui.screens.splash.MustUpdate
import jsanzo.movies.ui.screens.splash.SplashLoading
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
    private val mockedMustUpdateUseCase: MustUpdateUseCase = mockk()
    private val mockedTracker: MovieTracker = mockk(relaxed = true)
    private lateinit var splashViewModel: SplashViewModel

    private val actualVersion = "1.0.0"

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        splashViewModel = SplashViewModel(mockedMustUpdateUseCase, mockedTracker)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given no action, When state is observed, Then initial state is SplashLoading`() = runTest {
        splashViewModel.state.test {
            awaitItem() shouldBe SplashLoading
        }
    }

    @Test
    fun `Given any state, When trackScreenView is called, Then trackSplashShown is called`() = runTest {
        splashViewModel.state.test {
            awaitItem() shouldBe SplashLoading

            splashViewModel.trackScreenView()

            coVerify(exactly = 1) { mockedTracker.trackSplashShown() }
            confirmVerified(mockedTracker)
        }
    }

    @Test
    fun `Given use case returns true, When mustUpdate is called, Then state is MustUpdate`() = runTest {
        coEvery { mockedMustUpdateUseCase(actualVersion) } returns true.right()

        splashViewModel.state.test {
            awaitItem() shouldBe SplashLoading

            splashViewModel.mustUpdate(actualVersion)

            awaitItem() shouldBe MustUpdate

            coVerify(exactly = 1) { mockedMustUpdateUseCase(actualVersion) }
            confirmVerified(mockedMustUpdateUseCase)
        }
    }

    @Test
    fun `Given use case returns true, When mustUpdate is called, Then trackForceUpdateShown is called`() = runTest {
        coEvery { mockedMustUpdateUseCase(actualVersion) } returns true.right()

        splashViewModel.state.test {
            awaitItem() shouldBe SplashLoading

            splashViewModel.mustUpdate(actualVersion)

            awaitItem() shouldBe MustUpdate

            coVerify(exactly = 1) { mockedMustUpdateUseCase(actualVersion) }
            coVerify(exactly = 1) { mockedTracker.trackForceUpdateShown(actualVersion) }
            confirmVerified(mockedMustUpdateUseCase, mockedTracker)
        }
    }

    @Test
    fun `Given use case returns false, When mustUpdate is called, Then state is UpToDate`() = runTest {
        coEvery { mockedMustUpdateUseCase(actualVersion) } returns false.right()

        splashViewModel.state.test {
            awaitItem() shouldBe SplashLoading

            splashViewModel.mustUpdate(actualVersion)

            awaitItem() shouldBe UpToDate

            coVerify(exactly = 1) { mockedMustUpdateUseCase(actualVersion) }
            confirmVerified(mockedMustUpdateUseCase)
        }
    }

    @Test
    fun `Given use case returns error, When mustUpdate is called, Then state is UpToDate`() = runTest {
        coEvery { mockedMustUpdateUseCase(actualVersion) } returns mockk<MovieError>().left()

        splashViewModel.state.test {
            awaitItem() shouldBe SplashLoading

            splashViewModel.mustUpdate(actualVersion)

            awaitItem() shouldBe UpToDate

            coVerify(exactly = 1) { mockedMustUpdateUseCase(actualVersion) }
            confirmVerified(mockedMustUpdateUseCase)
        }
    }

    @Test
    fun `Given use case returns error, When mustUpdate is called, Then trackRemoteConfigError is called`() = runTest {
        coEvery { mockedMustUpdateUseCase(actualVersion) } returns mockk<MovieError>().left()

        splashViewModel.state.test {
            awaitItem() shouldBe SplashLoading

            splashViewModel.mustUpdate(actualVersion)

            awaitItem() shouldBe UpToDate

            coVerify(exactly = 1) { mockedMustUpdateUseCase(actualVersion) }
            coVerify(exactly = 1) { mockedTracker.trackRemoteConfigError() }
            confirmVerified(mockedMustUpdateUseCase, mockedTracker)
        }
    }
}
