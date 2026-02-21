package jsanzo.movies.ui.home

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import jsanzo.movies.common.EMPTY_STRING
import jsanzo.movies.domain.entity.Movie
import jsanzo.movies.domain.entity.MovieResult
import jsanzo.movies.domain.error.InvalidParametersError
import jsanzo.movies.domain.usecase.GetMoviesUseCase
import jsanzo.movies.domain.usecase.SaveMovieUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class HomeViewModelTest {

    private lateinit var homeViewModel: HomeViewModel

    @Mock
    private val mockedGetMoviesUseCase = Mockito.mock(GetMoviesUseCase::class.java)

    @Mock
    private val mockedSaveMovieUseCase = Mockito.mock(SaveMovieUseCase::class.java)

    @Spy
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

    @Before
    fun setUp() {
        runBlocking {
            Mockito.`when`(mockedGetMoviesUseCase(validPage)).thenReturn(flowOf(mockedMovie).right())
            Mockito.`when`(mockedGetMoviesUseCase(validPage + 1)).thenReturn(flowOf(mockedMovie).right())
            Mockito.`when`(mockedGetMoviesUseCase(invalidPage)).thenReturn(InvalidParametersError.left())
            Mockito.`when`(mockedSaveMovieUseCase(mockedMovieResult)).thenReturn(None)
            Mockito.`when`(mockedSaveMovieUseCase(invalidMockedMovieResult)).thenReturn(InvalidParametersError.some())
        }

        homeViewModel = HomeViewModel(mockedGetMoviesUseCase, mockedSaveMovieUseCase)
        homeViewModelStateFlow = homeViewModel.homeViewModelSateFlow
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `we are always in InitialState at the beginning`() {
        Assert.assertTrue(homeViewModelStateFlow.value is InitialState)
    }

    @Test
    fun `we are in MoviesRetrieved state after call getMovies() with valid page, also we get the movie list`() = runTest {
        homeViewModel.getMovies(validPage)

        verify(mockedGetMoviesUseCase, times(1))(validPage)

        assertTrue(homeViewModelStateFlow.value is MoviesRetrieved)

        val state = homeViewModelStateFlow.value as? MoviesRetrieved

        assertNotNull(state)
        assertEquals(mockedMovie.results, state?.movies)
    }

    @Test
    fun `we are in ErrorInOperation state after call getMovies() with invalid page`() = runTest {
        homeViewModel.getMovies(invalidPage)

        verify(mockedGetMoviesUseCase, times(1))(invalidPage)

        assertTrue(homeViewModelStateFlow.value is ErrorInOperation)
    }

    @Test
    fun `we are in SavedMovie state after call saveMovie() with valid movie result`() = runTest {
        homeViewModel.saveMovie(mockedMovieResult)

        verify(mockedSaveMovieUseCase, times(1))(mockedMovieResult)

        assertTrue(homeViewModelStateFlow.value is SavedMovie)
    }

    @Test
    fun `we are in ErrorInOperation state after call saveMovie() with invalid movie result`() = runTest {
        homeViewModel.saveMovie(invalidMockedMovieResult)

        verify(mockedSaveMovieUseCase, times(1))(invalidMockedMovieResult)

        Assert.assertTrue(homeViewModelStateFlow.value is ErrorInOperation)
    }

    @Test
    fun `we are in the initial state after call onStop()`() {
        homeViewModel.onStop()
        assertTrue(homeViewModelStateFlow.value is InitialState)
    }

    @Test
    fun `getMovies() called after notifyLastElementVisible() when we need new page with movies`() = runTest {
        homeViewModel.notifyLastElementVisible(lastElementVisibleToNeedMore)

        verify(mockedGetMoviesUseCase, times(1))(validPage + 1)

        assertTrue(homeViewModelStateFlow.value is MoviesRetrieved)

        val state = homeViewModelStateFlow.value as? MoviesRetrieved

        assertNotNull(state)
        assertEquals(mockedMovie.results, state?.movies)
    }

    @Test
    fun `getMovies() not called more after notifyLastElementVisible() called when we not need new page with movies`() = runTest {
        homeViewModel.getMovies(validPage)
        verify(mockedGetMoviesUseCase, times(1))(validPage)

        homeViewModel.notifyLastElementVisible(lastElementVisibleToNotNeedMore)
        verify(mockedGetMoviesUseCase, times(1))(validPage + 1)

        assertTrue(homeViewModelStateFlow.value is MoviesRetrieved)
    }
}
