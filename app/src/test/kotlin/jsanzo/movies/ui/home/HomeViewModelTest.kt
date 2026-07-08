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
import jsanzo.movies.ui.model.listOfDomainMovie
import jsanzo.movies.ui.model.listOfDomainMovie2
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
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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
    private val lastElementVisibleToNeedMore = 10
    private val lastElementVisibleToNotNeedMore = 1
    private val searchQuery = "harry potter"
    private val searchResults = listOf(domainMovie)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockedGetLayoutModeUseCase() } returns flowOf(DomainLayoutModePreference.Grid2)

        homeViewModel = HomeViewModel(
            getMoviesUseCase = mockedGetMoviesUseCase,
            saveMovieUseCase = mockedSaveMovieUseCase,
            searchMoviesUseCase = mockedSearchMoviesUseCase,
            getLayoutModeUseCase = mockedGetLayoutModeUseCase,
            saveLayoutModeUseCase = mockedSaveLayoutModeUseCase,
            firebaseTracker = mockedMovieTracker,
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

        verify(exactly = 1) { mockedGetLayoutModeUseCase.invoke() }
    }

    @Test
    fun `Given valid page, When getMovies is called, Then state is MovieListComplete`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns listOfDomainMovie.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies()

            awaitItem() shouldBe MovieListComplete(listOfDomainMovie.toMovieUi())

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            confirmVerified(mockedGetMoviesUseCase)
        }
    }

    @Test
    fun `Given use case returns error, When getMovies is called, Then state is MoviesError`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns NotFoundError.left()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies()

            awaitItem() shouldBe MoviesError(NotFoundError.toString())

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            confirmVerified(mockedGetMoviesUseCase)
        }
    }

    @Test
    fun `Given movies already loaded, When getMovies is called again, Then use case is not called again`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns listOfDomainMovie.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies()

            awaitItem() shouldBe MovieListComplete(listOfDomainMovie.toMovieUi())

            homeViewModel.getMovies()
            advanceUntilIdle()

            expectNoEvents()

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 1) { mockedMovieTracker.trackPageLoaded(validPage) }
            confirmVerified(mockedGetMoviesUseCase, mockedMovieTracker)
        }
    }

    @Test
    fun `Given loading in progress, When getMovies is called again, Then use case is called only once`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } coAnswers {
            delay(1.seconds)
            listOfDomainMovie.right()
        }

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies()
            runCurrent()
            homeViewModel.getMovies()
            advanceUntilIdle()

            awaitItem() shouldBe MovieListComplete(listOfDomainMovie.toMovieUi())

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 1) { mockedMovieTracker.trackPageLoaded(validPage) }
            confirmVerified(mockedGetMoviesUseCase, mockedMovieTracker)
        }
    }

    @Test
    fun `Given movies loaded, When last element visible reaches threshold, Then next page is loaded`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns listOfDomainMovie.right()
        coEvery { mockedGetMoviesUseCase(validPage + 1) } returns listOfDomainMovie2.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies()

            awaitItem() shouldBe MovieListComplete(listOfDomainMovie.toMovieUi())

            homeViewModel.notifyLastElementVisible(lastElementVisibleToNeedMore)

            awaitItem() shouldBe MovieListComplete((listOfDomainMovie + listOfDomainMovie2).toMovieUi())

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage + 1) }
            confirmVerified(mockedGetMoviesUseCase)
        }
    }

    @Test
    fun `Given movies loaded, When last element visible does not reach threshold, Then next page is not loaded`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns listOfDomainMovie.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies()

            awaitItem() shouldBe MovieListComplete(listOfDomainMovie.toMovieUi())

            homeViewModel.notifyLastElementVisible(lastElementVisibleToNotNeedMore)

            expectNoEvents()

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 0) { mockedGetMoviesUseCase(validPage + 1) }
            confirmVerified(mockedGetMoviesUseCase)
        }
    }

    @Test
    fun `Given same element notified twice, When notifyLastElementVisible is called, Then next page is loaded only once`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns listOfDomainMovie.right()
        coEvery { mockedGetMoviesUseCase(validPage + 1) } returns listOfDomainMovie2.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies()

            awaitItem() shouldBe MovieListComplete(listOfDomainMovie.toMovieUi())

            homeViewModel.notifyLastElementVisible(lastElementVisibleToNeedMore)
            advanceUntilIdle()

            awaitItem() shouldBe MovieListComplete((listOfDomainMovie + listOfDomainMovie2).toMovieUi())

            homeViewModel.notifyLastElementVisible(lastElementVisibleToNeedMore)
            advanceUntilIdle()

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage + 1) }
            coVerify(exactly = 0) { mockedGetMoviesUseCase(validPage + 2) }
            confirmVerified(mockedGetMoviesUseCase)
        }
    }

    @Test
    fun `Given page is not 1, When pagination is triggered, Then state does not change to Loading`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns listOfDomainMovie.right()
        coEvery { mockedGetMoviesUseCase(validPage + 1) } returns listOfDomainMovie2.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies()

            awaitItem() shouldBe MovieListComplete(listOfDomainMovie.toMovieUi())

            homeViewModel.notifyLastElementVisible(lastElementVisibleToNeedMore)

            // No Loading emitted — goes directly to MovieListComplete
            awaitItem() shouldBe MovieListComplete((listOfDomainMovie + listOfDomainMovie2).toMovieUi())

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage + 1) }
            confirmVerified(mockedGetMoviesUseCase)
        }
    }

    @Test
    fun `Given movies loaded, When saveMovie is called, Then state does not change`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns listOfDomainMovie.right()
        coEvery { mockedSaveMovieUseCase(domainMovie) } returns InvalidParametersError.some()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies()

            awaitItem() shouldBe MovieListComplete(listOfDomainMovie.toMovieUi())

            homeViewModel.saveMovie(movieUi)
            advanceUntilIdle()

            expectNoEvents()
        }

        coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
        coVerify(exactly = 1) { mockedSaveMovieUseCase(domainMovie) }
        confirmVerified(mockedGetMoviesUseCase, mockedSaveMovieUseCase)
    }

    @Test
    fun `Given movies loaded, When saveMovie is called, Then trackMovieClicked is called`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns listOfDomainMovie.right()
        coEvery { mockedSaveMovieUseCase(domainMovie) } returns InvalidParametersError.some()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies()

            awaitItem() shouldBe MovieListComplete(listOfDomainMovie.toMovieUi())

            homeViewModel.saveMovie(movieUi)
            advanceUntilIdle()
            expectNoEvents()

            verify(exactly = 1) { mockedMovieTracker.trackPageLoaded(validPage) }
            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            verify(exactly = 1) { mockedMovieTracker.trackMovieClicked(movieUi.id, movieUi.title) }
            coVerify(exactly = 1) { mockedSaveMovieUseCase(domainMovie) }
            confirmVerified(mockedMovieTracker, mockedSaveMovieUseCase)
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
    fun `Given valid search query, When onSearchQueryChange is called, Then state is MoviesSearch`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns listOfDomainMovie.right()
        coEvery { mockedSearchMoviesUseCase(searchQuery) } returns searchResults.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies()

            awaitItem() shouldBe MovieListComplete(listOfDomainMovie.toMovieUi())

            homeViewModel.onSearchQueryChange(searchQuery)
            testDispatcher.scheduler.advanceTimeBy(501.milliseconds)

            awaitItem() shouldBe Loading
            awaitItem() shouldBe MoviesSearch(searchResults.toMovieUi())

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 1) { mockedSearchMoviesUseCase(searchQuery) }
            confirmVerified(mockedGetMoviesUseCase, mockedSearchMoviesUseCase)
        }
    }

    @Test
    fun `Given search fails, When onSearchQueryChange is called, Then state is MoviesError`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns listOfDomainMovie.right()
        coEvery { mockedSearchMoviesUseCase(searchQuery) } returns InvalidParametersError.left()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies()

            awaitItem() shouldBe MovieListComplete(listOfDomainMovie.toMovieUi())

            homeViewModel.onSearchQueryChange(searchQuery)
            testDispatcher.scheduler.advanceTimeBy(501.milliseconds)

            awaitItem() shouldBe Loading
            awaitItem() shouldBe MoviesError(InvalidParametersError.toString())

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 1) { mockedSearchMoviesUseCase(searchQuery) }
            confirmVerified(mockedGetMoviesUseCase, mockedSearchMoviesUseCase)
        }
    }

    @Test
    fun `Given search fails, When onSearchQueryChange is called, Then trackErrorShown is called`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns listOfDomainMovie.right()
        coEvery { mockedSearchMoviesUseCase(searchQuery) } returns InvalidParametersError.left()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies()

            awaitItem() shouldBe MovieListComplete(listOfDomainMovie.toMovieUi())

            homeViewModel.onSearchQueryChange(searchQuery)
            testDispatcher.scheduler.advanceTimeBy(501.milliseconds)

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
        coEvery { mockedGetMoviesUseCase(validPage) } returns listOfDomainMovie.right()
        coEvery { mockedSearchMoviesUseCase(searchQuery) } returns searchResults.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies()

            awaitItem() shouldBe MovieListComplete(listOfDomainMovie.toMovieUi())

            homeViewModel.onSearchQueryChange(searchQuery)
            testDispatcher.scheduler.advanceTimeBy(501.milliseconds)

            awaitItem() shouldBe Loading
            awaitItem() shouldBe MoviesSearch(searchResults.toMovieUi())

            homeViewModel.onSearchQueryChange("")
            testDispatcher.scheduler.advanceTimeBy(501.milliseconds)

            awaitItem() shouldBe MovieListComplete(listOfDomainMovie.toMovieUi())

            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            coVerify(exactly = 1) { mockedSearchMoviesUseCase(searchQuery) }
            confirmVerified(mockedGetMoviesUseCase, mockedSearchMoviesUseCase)
        }
    }

    @Test
    fun `Given search active, When notifyLastElementVisible is called, Then getMovies is not triggered`() = runTest {
        coEvery { mockedGetMoviesUseCase(validPage) } returns listOfDomainMovie.right()
        coEvery { mockedSearchMoviesUseCase(searchQuery) } returns searchResults.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies()

            awaitItem() shouldBe MovieListComplete(listOfDomainMovie.toMovieUi())

            homeViewModel.onSearchQueryChange(searchQuery)
            testDispatcher.scheduler.advanceTimeBy(501.milliseconds)

            awaitItem() shouldBe Loading
            awaitItem() shouldBe MoviesSearch(searchResults.toMovieUi())

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
        coEvery { mockedGetMoviesUseCase(validPage) } returns listOfDomainMovie.right()
        coEvery { mockedSearchMoviesUseCase(searchQuery) } returns searchResults.right()

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.getMovies()

            awaitItem() shouldBe MovieListComplete(listOfDomainMovie.toMovieUi())

            homeViewModel.onSearchQueryChange(searchQuery)
            testDispatcher.scheduler.advanceTimeBy(501.milliseconds)

            awaitItem() shouldBe Loading
            awaitItem() shouldBe MoviesSearch(searchResults.toMovieUi())

            verify(exactly = 1) { mockedMovieTracker.trackPageLoaded(validPage) }
            coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
            verify(exactly = 1) { mockedMovieTracker.trackSearchPerformed(searchQuery, searchResults.size) }
            coVerify(exactly = 1) { mockedSearchMoviesUseCase(searchQuery) }
            confirmVerified(mockedGetMoviesUseCase, mockedSearchMoviesUseCase, mockedMovieTracker)
        }
    }

    @Test
    fun `Given any layout mode, When saveLayoutMode is called, Then trackLayoutModeChanged is called`() = runTest {
        coEvery { mockedSaveLayoutModeUseCase(DomainLayoutModePreference.Grid3) } just Runs

        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.saveLayoutMode(LayoutModeUi.Grid3)
            advanceUntilIdle()

            coVerify(exactly = 1) { mockedMovieTracker.trackLayoutModeChanged(LayoutModeUi.Grid3.name) }
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

    @Test
    fun `Given same layout mode, When saveLayoutMode is called, Then tracker and use case are not called`() = runTest {
        homeViewModel.state.test {
            awaitItem() shouldBe Loading

            homeViewModel.saveLayoutMode(LayoutModeUi.Grid2) // Grid2 es el valor inicial
            advanceUntilIdle()

            verify(exactly = 0) { mockedMovieTracker.trackLayoutModeChanged(any()) }
            coVerify(exactly = 0) { mockedSaveLayoutModeUseCase(any()) }
            confirmVerified(mockedMovieTracker, mockedSaveLayoutModeUseCase)
        }
    }
}
