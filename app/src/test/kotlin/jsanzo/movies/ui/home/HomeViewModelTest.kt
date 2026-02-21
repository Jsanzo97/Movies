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
import jsanzo.movies.common.EMPTY_STRING
import jsanzo.movies.domain.entity.Movie
import jsanzo.movies.domain.entity.MovieResult
import jsanzo.movies.domain.error.InvalidParametersError
import jsanzo.movies.domain.usecase.GetMoviesUseCase
import jsanzo.movies.domain.usecase.SaveMovieUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
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

    private val mockedGetMoviesUseCase: GetMoviesUseCase = mockk()
    private val mockedSaveMovieUseCase: SaveMovieUseCase = mockk()

    private lateinit var homeViewModelStateFlow: StateFlow<HomeViewState>

    private val validPage = 1
    private val invalidPage = -1
    private val lastElementVisibleToNeedMore = 10
    private val lastElementVisibleToNotNeedMore = 1

    private val invalidMockedMovieResult = MovieResult(
        posterPath = null,
        adult = false,
        overview = EMPTY_STRING,
        releaseDate = EMPTY_STRING,
        genreIds = listOf(),
        id = -1,
        originalTitle = EMPTY_STRING,
        originalLanguage = EMPTY_STRING,
        title = EMPTY_STRING,
        backdropPath = null,
        popularity = 0.0,
        voteCount = 0,
        video = false,
        voteAverage = 0.0,
    )

    private val mockedMovieResult = MovieResult(
        posterPath = null,
        adult = false,
        overview = EMPTY_STRING,
        releaseDate = EMPTY_STRING,
        genreIds = listOf(),
        id = 0,
        originalTitle = EMPTY_STRING,
        originalLanguage = EMPTY_STRING,
        title = EMPTY_STRING,
        backdropPath = null,
        popularity = 0.0,
        voteCount = 0,
        video = false,
        voteAverage = 0.0,
    )

    private val mockedMovie = Movie(
        page = 0,
        results = listOf(mockedMovieResult, mockedMovieResult),
        totalResults = 0,
        totalPages = 0,
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockedGetMoviesUseCase(validPage) } returns flowOf(mockedMovie).right()
        coEvery { mockedGetMoviesUseCase(validPage + 1) } returns flowOf(mockedMovie).right()
        coEvery { mockedGetMoviesUseCase(invalidPage) } returns InvalidParametersError.left()
        coEvery { mockedSaveMovieUseCase(mockedMovieResult) } returns None
        coEvery { mockedSaveMovieUseCase(invalidMockedMovieResult) } returns InvalidParametersError.some()

        homeViewModel = HomeViewModel(mockedGetMoviesUseCase, mockedSaveMovieUseCase)
        homeViewModelStateFlow = homeViewModel.homeViewModelSateFlow
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `we are always in InitialState at the beginning`() {
        homeViewModelStateFlow.value.shouldBeInstanceOf<InitialState>()
    }

    @Test
    fun `we are in MoviesRetrieved state after call getMovies() with valid page, also we get the movie list`() = runTest {
        homeViewModel.getMovies(validPage)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }

        homeViewModelStateFlow.value.shouldBeInstanceOf<MoviesRetrieved>()

        val state = homeViewModelStateFlow.value as? MoviesRetrieved

        state?.movies shouldBe mockedMovie.results
    }

    @Test
    fun `we are in ErrorInOperation state after call getMovies() with invalid page`() = runTest {
        homeViewModel.getMovies(invalidPage)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedGetMoviesUseCase(invalidPage) }

        homeViewModelStateFlow.value.shouldBeInstanceOf<ErrorInOperation>()
    }

    @Test
    fun `we are in SavedMovie state after call saveMovie() with valid movie result`() = runTest {
        homeViewModel.saveMovie(mockedMovieResult)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedSaveMovieUseCase(mockedMovieResult) }

        homeViewModelStateFlow.value.shouldBeInstanceOf<SavedMovie>()
    }

    @Test
    fun `we are in ErrorInOperation state after call saveMovie() with invalid movie result`() = runTest {
        homeViewModel.saveMovie(invalidMockedMovieResult)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedSaveMovieUseCase(invalidMockedMovieResult) }

        homeViewModelStateFlow.value.shouldBeInstanceOf<ErrorInOperation>()
    }

    @Test
    fun `we are in the initial state after call onStop()`() {
        homeViewModel.onStop()
        homeViewModelStateFlow.value.shouldBeInstanceOf<InitialState>()
    }

    @Test
    fun `getMovies() called after notifyLastElementVisible() when we need new page with movies`() = runTest {
        homeViewModel.notifyLastElementVisible(lastElementVisibleToNeedMore)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage + 1) }

        homeViewModelStateFlow.value.shouldBeInstanceOf<MoviesRetrieved>()

        val state = homeViewModelStateFlow.value as? MoviesRetrieved

        state?.movies shouldBe mockedMovie.results
    }

    @Test
    fun `getMovies() not called more after notifyLastElementVisible() called when we not need new page with movies`() = runTest {
        homeViewModel.getMovies(validPage)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage) }

        homeViewModel.notifyLastElementVisible(lastElementVisibleToNotNeedMore)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify(exactly = 1) { mockedGetMoviesUseCase(validPage + 1) }

        homeViewModelStateFlow.value.shouldBeInstanceOf<MoviesRetrieved>()
    }
}
