package jsanzo.movies.ui.home

import app.cash.turbine.test
import arrow.core.left
import arrow.core.right
import arrow.core.some
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import jsanzo.movies.domain.error.InvalidParametersError
import jsanzo.movies.domain.error.NotFoundError
import jsanzo.movies.domain.model.DomainLayoutModePreference
import jsanzo.movies.domain.usecase.GetLayoutModeUseCase
import jsanzo.movies.domain.usecase.GetMoviesUseCase
import jsanzo.movies.domain.usecase.SaveLayoutModeUseCase
import jsanzo.movies.domain.usecase.SaveMovieUseCase
import jsanzo.movies.domain.usecase.SearchMoviesUseCase
import jsanzo.movies.presentation.HomeViewModel
import jsanzo.movies.tracking.MovieTracker
import jsanzo.movies.ui.model.domainMovie
import jsanzo.movies.ui.model.domainMovie2
import jsanzo.movies.ui.model.domainMovieResult
import jsanzo.movies.ui.model.movieUi
import jsanzo.movies.ui.screens.home.LayoutModeUi
import jsanzo.movies.ui.screens.home.Loading
import jsanzo.movies.ui.screens.home.MovieListComplete
import jsanzo.movies.ui.screens.home.MoviesError
import jsanzo.movies.ui.screens.home.MoviesSearch
import jsanzo.movies.ui.screens.home.toMovieUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var homeViewModel: HomeViewModel

    private val mockedGetMoviesUseCase: GetMoviesUseCase = mockk()
    private val mockedSaveMovieUseCase: SaveMovieUseCase = mockk()
    private val mockedSearchMoviesUseCase: SearchMoviesUseCase = mockk()
    private val mockedMovieTracker: MovieTracker = mockk(relaxed = true)
    private val mockedGetLayoutModeUseCase: GetLayoutModeUseCase = mockk()
    private val mockedSaveLayoutModeUseCase: SaveLayoutModeUseCase = mockk()

    private val validPage = 1
    private val invalidPage = -1
    private val lastElementVisibleToNeedMore = 10
    private val lastElementVisibleToNotNeedMore = 1
    private val searchQuery = "harry potter"

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockedGetLayoutModeUseCase() } returns flowOf(DomainLayoutModePreference.Grid2)

        homeViewModel = HomeViewModel(
            mockedGetMoviesUseCase,
            mockedSaveMovieUseCase,
            mockedSearchMoviesUseCase,
            mockedGetLayoutModeUseCase,
            mockedSaveLayoutModeUseCase,
            mockedMovieTracker,
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Given no action, When state is observed, Then initial state is Loading`() = runTest {
        homeViewModel.state.test {
            awaitItem() shouldBe Loading
        }
    }

    @Test
    fun `Given viewModel is created, When layoutMode is observed, Then initial value is Grid2`() = runTest {
        homeViewModel.layoutMode.test {
            awaitItem() shouldBe LayoutModeUi.Grid2
        }

        verify(exactly = 1) { mockedGetLayoutModeUseCase() }
        confirmVerified(mockedGetLayoutModeUseCase)
    }

    @Test
    fun `Given valid page, When getMovies is called, Then state is MovieListComplete`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns domainMovie.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies(validPage)

            awaitItem() shouldBe MovieListComplete(domainMovie.results.toMovieUi())

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            confirmVerified(mockedGetMoviesUseCase)
        }
    }

    @Test
    fun `Given invalid page, When getMovies is called, Then state is MoviesError`() = runTest {
        coEvery { mockedGetMoviesUseCase(invalidPage) } returns NotFoundError.left()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies(invalidPage)

            awaitItem() shouldBe MoviesError(NotFoundError.toString())

            coVerify(exactly = 1) { mockedGetMoviesUseCase(invalidPage) }
            confirmVerified(mockedGetMoviesUseCase)
        }
    }

    @Test
    fun `Given movies loaded, When saveMovie is called, Then state does not change`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns domainMovie.right()
        coEvery { mockedSaveMovieUseCase(domainMovieResult) } returns InvalidParametersError.some()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies(validPage)

            awaitItem() shouldBe MovieListComplete(domainMovie.results.toMovieUi())

            homeViewModel.saveMovie(movieUi)
            advanceUntilIdle()

            expectNoEvents()
        }

        coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
        coVerify(exactly = 1) { mockedSaveMovieUseCase(domainMovieResult) }
        confirmVerified(mockedGetMoviesUseCase, mockedSaveMovieUseCase)
    }

    @Test
    fun `Given movies loaded, When last element visible reaches threshold, Then next page is loaded`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns domainMovie.right()
        coEvery { mockedGetMoviesUseCase(validPage + 1) } returns domainMovie2.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies(validPage)

            awaitItem() shouldBe MovieListComplete(domainMovie.results.toMovieUi())

            homeViewModel.notifyLastElementVisible(lastElementVisibleToNeedMore)

            awaitItem() shouldBe MovieListComplete((domainMovie.results + domainMovie2.results).toMovieUi())

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage + 1) }
            confirmVerified(mockedGetMoviesUseCase)
        }
    }

    @Test
    fun `Given movies loaded, When last element visible does not reach threshold, Then next page is not loaded`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns domainMovie.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies(validPage)

            awaitItem() shouldBe MovieListComplete(domainMovie.results.toMovieUi())

            homeViewModel.notifyLastElementVisible(lastElementVisibleToNotNeedMore)

            expectNoEvents()

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 0) { mockedGetMoviesUseCase(validPage + 1) }
            confirmVerified(mockedGetMoviesUseCase)
        }
    }

    @Test
    fun `Given any state, When trackScreenView is called, Then trackHomeShown is called`() = runTest {
        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.trackScreenView()

            verify(exactly = 1) { mockedMovieTracker.trackHomeShown() }
        }
    }

    @Test
    fun `Given movies loaded, When saveMovie is called, Then trackMovieClicked is called`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns domainMovie.right()
        coEvery { mockedSaveMovieUseCase(domainMovieResult) } returns InvalidParametersError.some()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies(validPage)

            awaitItem() shouldBe MovieListComplete(domainMovie.results.toMovieUi())

            homeViewModel.saveMovie(movieUi)
            advanceUntilIdle()
            expectNoEvents()

            verify(exactly = 1) { mockedMovieTracker.trackPageLoaded(validPage) }
            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            verify(exactly = 1) { mockedMovieTracker.trackMovieClicked(domainMovieResult.id, domainMovieResult.title) }
            coVerify(exactly = 1) { mockedSaveMovieUseCase(domainMovieResult) }
            confirmVerified(mockedMovieTracker, mockedSaveMovieUseCase)
        }
    }

    @Test
    fun `Given loading in progress, When getMovies is called again, Then use case is called only once`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } coAnswers {
            delay(1_000)
            domainMovie.right()
        }

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies(validPage)
            runCurrent()
            homeViewModel.getMovies(validPage)
            advanceUntilIdle()

            awaitItem() shouldBe MovieListComplete(domainMovie.results.toMovieUi())

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 1) { mockedMovieTracker.trackPageLoaded(validPage) }
            confirmVerified(mockedGetMoviesUseCase, mockedMovieTracker)
        }
    }

    @Test
    fun `Given page is not 1, When getMovies is called, Then state does not change to Loading`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns domainMovie.right()
        coEvery { mockedGetMoviesUseCase(validPage + 1) } returns domainMovie2.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies(validPage)

            awaitItem() shouldBe MovieListComplete(domainMovie.results.toMovieUi())

            homeViewModel.getMovies(validPage + 1)

            awaitItem() shouldBe MovieListComplete((domainMovie.results + domainMovie2.results).toMovieUi())

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage + 1) }
            confirmVerified(mockedGetMoviesUseCase)
        }
    }

    @Test
    fun `Given same element notified twice, When notifyLastElementVisible is called, Then next page is loaded only once`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns domainMovie.right()
        coEvery { mockedGetMoviesUseCase(validPage + 1) } returns domainMovie2.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies(validPage)

            awaitItem() shouldBe MovieListComplete(domainMovie.results.toMovieUi())

            homeViewModel.notifyLastElementVisible(lastElementVisibleToNeedMore)
            advanceUntilIdle()

            awaitItem() shouldBe MovieListComplete((domainMovie.results + domainMovie2.results).toMovieUi())

            homeViewModel.notifyLastElementVisible(lastElementVisibleToNeedMore)
            advanceUntilIdle()

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage + 1) }
            coVerify(exactly = 0) { mockedGetMoviesUseCase(validPage + 2) }
            confirmVerified(mockedGetMoviesUseCase)
        }
    }

    @Test
    fun `Given valid search query, When onSearchQueryChange is called, Then state is MoviesSearch`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns domainMovie.right()
        coEvery { mockedSearchMoviesUseCase(searchQuery) } returns domainMovie.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies(validPage)

            awaitItem() shouldBe MovieListComplete(domainMovie.results.toMovieUi())

            homeViewModel.onSearchQueryChange(searchQuery)

            awaitItem() shouldBe Loading
            awaitItem() shouldBe MoviesSearch(domainMovie.results.toMovieUi())

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 1) { mockedSearchMoviesUseCase(searchQuery) }
            confirmVerified(mockedGetMoviesUseCase, mockedSearchMoviesUseCase)
        }
    }

    @Test
    fun `Given search fails, When onSearchQueryChange is called, Then state is MoviesError`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns domainMovie.right()
        coEvery { mockedSearchMoviesUseCase(searchQuery) } returns InvalidParametersError.left()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies(validPage)

            awaitItem() shouldBe MovieListComplete(domainMovie.results.toMovieUi())

            homeViewModel.onSearchQueryChange(searchQuery)

            awaitItem() shouldBe Loading
            awaitItem() shouldBe MoviesError(InvalidParametersError.toString())

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 1) { mockedSearchMoviesUseCase(searchQuery) }
            confirmVerified(mockedGetMoviesUseCase, mockedSearchMoviesUseCase)
        }
    }

    @Test
    fun `Given search fails, When onSearchQueryChange is called, Then trackErrorShown is called`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns domainMovie.right()
        coEvery { mockedSearchMoviesUseCase(searchQuery) } returns InvalidParametersError.left()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies(validPage)

            awaitItem() shouldBe MovieListComplete(domainMovie.results.toMovieUi())

            homeViewModel.onSearchQueryChange(searchQuery)

            awaitItem() shouldBe Loading
            awaitItem() shouldBe MoviesError(InvalidParametersError.toString())

            verify(exactly = 1) { mockedMovieTracker.trackPageLoaded(validPage) }
            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            verify(exactly = 1) { mockedMovieTracker.trackErrorShown("home_search", any()) }
            coVerify(exactly = 1) { mockedSearchMoviesUseCase(searchQuery) }
            confirmVerified(mockedMovieTracker, mockedGetMoviesUseCase, mockedSearchMoviesUseCase)
        }
    }

    @Test
    fun `Given search active, When query is cleared, Then state restores MovieListComplete`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns domainMovie.right()
        coEvery { mockedSearchMoviesUseCase(searchQuery) } returns domainMovie.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies(validPage)

            awaitItem() shouldBe MovieListComplete(domainMovie.results.toMovieUi())

            homeViewModel.onSearchQueryChange(searchQuery)

            awaitItem() shouldBe Loading
            awaitItem() shouldBe MoviesSearch(domainMovie.results.toMovieUi())

            homeViewModel.onSearchQueryChange("")

            awaitItem() shouldBe MovieListComplete(domainMovie.results.toMovieUi())

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 1) { mockedSearchMoviesUseCase(searchQuery) }
            confirmVerified(mockedGetMoviesUseCase, mockedSearchMoviesUseCase)
        }
    }

    @Test
    fun `Given search active, When notifyLastElementVisible is called, Then getMovies is not triggered`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns domainMovie.right()
        coEvery { mockedSearchMoviesUseCase(searchQuery) } returns domainMovie.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies(validPage)

            awaitItem() shouldBe MovieListComplete(domainMovie.results.toMovieUi())

            homeViewModel.onSearchQueryChange(searchQuery)

            awaitItem() shouldBe Loading
            awaitItem() shouldBe MoviesSearch(domainMovie.results.toMovieUi())

            homeViewModel.notifyLastElementVisible(lastElementVisibleToNeedMore)

            expectNoEvents()

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 0) { mockedGetMoviesUseCase(validPage + 1) }
            coVerify(exactly = 1) { mockedSearchMoviesUseCase(searchQuery) }
            confirmVerified(mockedGetMoviesUseCase, mockedSearchMoviesUseCase)
        }
    }

    @Test
    fun `Given search succeeds, When onSearchQueryChange is called, Then trackSearchPerformed is called`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns domainMovie.right()
        coEvery { mockedSearchMoviesUseCase(searchQuery) } returns domainMovie.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies(validPage)

            awaitItem() shouldBe MovieListComplete(domainMovie.results.toMovieUi())

            homeViewModel.onSearchQueryChange(searchQuery)

            awaitItem() shouldBe Loading
            awaitItem() shouldBe MoviesSearch(domainMovie.results.toMovieUi())

            verify(exactly = 1) { mockedMovieTracker.trackPageLoaded(validPage) }
            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            verify(exactly = 1) { mockedMovieTracker.trackSearchPerformed(searchQuery, domainMovie.results.size) }
            coVerify(exactly = 1) { mockedSearchMoviesUseCase(searchQuery) }
            confirmVerified(mockedGetMoviesUseCase, mockedSearchMoviesUseCase, mockedMovieTracker)
        }
    }

    @Test
    fun `Given any layout mode, When saveLayoutMode is called, Then trackLayoutModeChanged is called`() = runTest {
        coEvery { mockedSaveLayoutModeUseCase(DomainLayoutModePreference.Grid2) } just Runs

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.saveLayoutMode(LayoutModeUi.Grid2)
            advanceUntilIdle()

            coVerify(exactly = 1) { mockedMovieTracker.trackLayoutModeChanged(LayoutModeUi.Grid2.name) }
            confirmVerified(mockedMovieTracker)
        }
    }

    @Test
    fun `Given any layout mode, When saveLayoutMode is called, Then saveLayoutModeUseCase is called`() = runTest {
        coEvery { mockedSaveLayoutModeUseCase(DomainLayoutModePreference.Grid3) } just Runs

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.saveLayoutMode(LayoutModeUi.Grid3)
            advanceUntilIdle()

            coVerify(exactly = 1) { mockedSaveLayoutModeUseCase(DomainLayoutModePreference.Grid3) }
            confirmVerified(mockedSaveLayoutModeUseCase)
        }
    }
}
