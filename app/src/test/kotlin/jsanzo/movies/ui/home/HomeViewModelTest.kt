package jsanzo.movies.ui.home

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import jsanzo.movies.domain.error.InvalidParametersError
import jsanzo.movies.domain.usecase.GetMoviesUseCase
import jsanzo.movies.domain.usecase.SaveMovieUseCase
import jsanzo.movies.domain.usecase.SearchMoviesUseCase
import jsanzo.movies.tracking.MovieTracker
import jsanzo.movies.ui.model.domainMovie
import jsanzo.movies.ui.model.domainMovieResult
import jsanzo.movies.ui.screens.home.HomeViewModel
import jsanzo.movies.ui.screens.home.HomeViewState
import jsanzo.movies.ui.screens.home.Loading
import jsanzo.movies.ui.screens.home.MoviesError
import jsanzo.movies.ui.screens.home.MoviesSuccess
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var homeViewModelStateFlow: StateFlow<HomeViewState>

    private val mockedGetMoviesUseCase: GetMoviesUseCase = mockk()
    private val mockedSaveMovieUseCase: SaveMovieUseCase = mockk()
    private val mockedSearchMoviesUseCase: SearchMoviesUseCase = mockk()
    private val mockedMovieTracker: MovieTracker = mockk(relaxed = true)

    private val validPage = 1
    private val invalidPage = -1
    private val lastElementVisibleToNeedMore = 10
    private val lastElementVisibleToNotNeedMore = 1
    private val searchQuery = "harry potter"

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockedGetMoviesUseCase(validPage) } returns domainMovie.right()
        coEvery { mockedGetMoviesUseCase(validPage + 1) } returns domainMovie.right()
        coEvery { mockedGetMoviesUseCase(invalidPage) } returns InvalidParametersError.left()
        coEvery { mockedSaveMovieUseCase(domainMovieResult) } returns None
        coEvery { mockedSaveMovieUseCase(any()) } returns InvalidParametersError.some()
        coEvery { mockedSearchMoviesUseCase(searchQuery) } returns domainMovie.right()

        homeViewModel = HomeViewModel(
            mockedGetMoviesUseCase,
            mockedSaveMovieUseCase,
            mockedSearchMoviesUseCase,
            mockedMovieTracker,
        )
        homeViewModelStateFlow = homeViewModel.state
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() {
        homeViewModelStateFlow.value.shouldBeInstanceOf<Loading>()
    }

    @Test
    fun `state is MoviesSuccess after getMovies() with valid page`() = runTest {
        homeViewModel.getMovies(validPage)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
        homeViewModelStateFlow.value.shouldBeInstanceOf<MoviesSuccess>()

        val state = homeViewModelStateFlow.value as MoviesSuccess
        state.movies shouldBe domainMovie.results
    }

    @Test
    fun `state is MoviesError after getMovies() with invalid page`() = runTest {
        homeViewModel.getMovies(invalidPage)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedGetMoviesUseCase(invalidPage) }
        homeViewModelStateFlow.value.shouldBeInstanceOf<MoviesError>()
    }

    @Test
    fun `state is MoviesError after saveMovie() fails`() = runTest {
        homeViewModel.getMovies(validPage)
        testDispatcher.scheduler.advanceUntilIdle()

        val previousState = homeViewModelStateFlow.value

        coEvery { mockedSaveMovieUseCase(domainMovieResult) } returns InvalidParametersError.some()
        homeViewModel.saveMovie(domainMovieResult)
        testDispatcher.scheduler.advanceUntilIdle()

        homeViewModelStateFlow.value shouldBe previousState
    }

    @Test
    fun `getMovies() called after notifyLastElementVisible() when threshold reached`() = runTest {
        homeViewModel.getMovies(validPage)
        testDispatcher.scheduler.advanceUntilIdle()

        homeViewModel.notifyLastElementVisible(lastElementVisibleToNeedMore)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockedGetMoviesUseCase(validPage + 1) }
        homeViewModelStateFlow.value.shouldBeInstanceOf<MoviesSuccess>()
    }

    @Test
    fun `getMovies() not called when threshold not reached`() = runTest {
        homeViewModel.getMovies(validPage)
        testDispatcher.scheduler.advanceUntilIdle()

        homeViewModel.notifyLastElementVisible(lastElementVisibleToNotNeedMore)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { mockedGetMoviesUseCase(validPage + 1) }
    }

    @Test
    fun `trackScreenView calls tracker trackHomeShown`() = runTest {
        homeViewModel.trackScreenView()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedMovieTracker.trackHomeShown() }
    }

    @Test
    fun `saveMovie calls tracker trackMovieClicked`() = runTest {
        homeViewModel.getMovies(validPage)
        testDispatcher.scheduler.advanceUntilIdle()

        homeViewModel.saveMovie(domainMovieResult)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedMovieTracker.trackMovieClicked(domainMovieResult.id, domainMovieResult.title) }
    }

    @Test
    fun `MoviesError contains correct message`() {
        val error = MoviesError("Something went wrong")
        error.message shouldBe "Something went wrong"
    }

    @Test
    fun `getMovies does nothing when a loading job is already active`() = runTest {
        homeViewModel.getMovies(validPage)
        homeViewModel.getMovies(validPage)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }
    }

    @Test
    fun `state does not change to Loading when page is not 1`() = runTest {
        homeViewModel.getMovies(validPage)
        testDispatcher.scheduler.advanceUntilIdle()

        val stateBeforeSecondLoad = homeViewModelStateFlow.value

        homeViewModel.getMovies(validPage + 1)
        stateBeforeSecondLoad.shouldBeInstanceOf<MoviesSuccess>()
    }

    @Test
    fun `notifyLastElementVisible does nothing when same element is notified twice`() = runTest {
        homeViewModel.getMovies(validPage)
        testDispatcher.scheduler.advanceUntilIdle()

        homeViewModel.notifyLastElementVisible(lastElementVisibleToNeedMore)
        testDispatcher.scheduler.advanceUntilIdle()

        homeViewModel.notifyLastElementVisible(lastElementVisibleToNeedMore)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { mockedGetMoviesUseCase(validPage + 2) }
    }

    @Test
    fun `state is MoviesSuccess after onSearchQueryChange with valid query`() = runTest {
        homeViewModel.onSearchQueryChange(searchQuery)
        testDispatcher.scheduler.advanceTimeBy(301)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedSearchMoviesUseCase(searchQuery) }
        homeViewModelStateFlow.value.shouldBeInstanceOf<MoviesSuccess>()

        val state = homeViewModelStateFlow.value as MoviesSuccess
        state.movies shouldBe domainMovie.results
    }

    @Test
    fun `state is MoviesError after onSearchQueryChange when search fails`() = runTest {
        coEvery { mockedSearchMoviesUseCase(searchQuery) } returns InvalidParametersError.left()

        homeViewModel.onSearchQueryChange(searchQuery)
        testDispatcher.scheduler.advanceTimeBy(301)
        testDispatcher.scheduler.advanceUntilIdle()

        homeViewModelStateFlow.value.shouldBeInstanceOf<MoviesError>()
    }

    @Test
    fun `trackErrorShown called with home_search when search fails`() = runTest {
        coEvery { mockedSearchMoviesUseCase(searchQuery) } returns InvalidParametersError.left()

        homeViewModel.onSearchQueryChange(searchQuery)
        testDispatcher.scheduler.advanceTimeBy(301)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedMovieTracker.trackErrorShown("home_search", any()) }
    }

    @Test
    fun `state restores moviesRetrieved when search query is cleared`() = runTest {
        homeViewModel.getMovies(validPage)
        testDispatcher.scheduler.advanceUntilIdle()

        homeViewModel.onSearchQueryChange(searchQuery)
        testDispatcher.scheduler.advanceTimeBy(301)
        testDispatcher.scheduler.advanceUntilIdle()

        homeViewModel.onSearchQueryChange("")
        testDispatcher.scheduler.advanceTimeBy(301)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = homeViewModelStateFlow.value as MoviesSuccess
        state.movies shouldBe domainMovie.results
    }

    @Test
    fun `getMovies not triggered by notifyLastElementVisible when search query is active`() = runTest {
        homeViewModel.getMovies(validPage)
        testDispatcher.scheduler.advanceUntilIdle()

        homeViewModel.onSearchQueryChange(searchQuery)
        testDispatcher.scheduler.advanceTimeBy(301)
        testDispatcher.scheduler.advanceUntilIdle()

        homeViewModel.notifyLastElementVisible(lastElementVisibleToNeedMore)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { mockedGetMoviesUseCase(validPage + 1) }
    }

    @Test
    fun `trackSearchPerformed called with query and results count on search success`() = runTest {
        homeViewModel.onSearchQueryChange(searchQuery)
        testDispatcher.scheduler.advanceTimeBy(301)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            mockedMovieTracker.trackSearchPerformed(searchQuery, domainMovie.results.size)
        }
    }
}
