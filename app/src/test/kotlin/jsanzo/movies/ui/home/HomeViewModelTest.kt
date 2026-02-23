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
import jsanzo.movies.domain.model.DomainMovie
import jsanzo.movies.domain.model.DomainMovieResult
import jsanzo.movies.domain.usecase.GetMoviesUseCase
import jsanzo.movies.domain.usecase.SaveMovieUseCase
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

    private val validPage = 1
    private val invalidPage = -1
    private val lastElementVisibleToNeedMore = 10
    private val lastElementVisibleToNotNeedMore = 1

    private val mockedDomainMovieResult = DomainMovieResult(
        posterPath = null,
        adult = false,
        overview = "",
        releaseDate = "",
        genreIds = listOf(),
        id = 1,
        originalTitle = "",
        originalLanguage = "",
        title = "",
        backdropPath = null,
        popularity = 0.0,
        voteCount = 0,
        video = false,
        voteAverage = 0.0,
    )

    private val mockedDomainMovie = DomainMovie(
        page = 0,
        results = (1..20).map { id ->
            mockedDomainMovieResult.copy(id = id)
        },
        totalResults = 0,
        totalPages = 0,
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockedGetMoviesUseCase(validPage) } returns mockedDomainMovie.right()
        coEvery { mockedGetMoviesUseCase(validPage + 1) } returns mockedDomainMovie.right()
        coEvery { mockedGetMoviesUseCase(invalidPage) } returns InvalidParametersError.left()
        coEvery { mockedSaveMovieUseCase(mockedDomainMovieResult) } returns None
        coEvery { mockedSaveMovieUseCase(any()) } returns InvalidParametersError.some()

        homeViewModel = HomeViewModel(mockedGetMoviesUseCase, mockedSaveMovieUseCase)
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
        state.movies shouldBe mockedDomainMovie.results
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

        coEvery { mockedSaveMovieUseCase(mockedDomainMovieResult) } returns InvalidParametersError.some()
        homeViewModel.saveMovie(mockedDomainMovieResult)
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
}
