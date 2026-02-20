package jsanzo.movies.ui.details

import arrow.core.left
import arrow.core.right
import jsanzo.movies.common.EMPTY_STRING
import jsanzo.movies.domain.entity.MovieDetails
import jsanzo.movies.domain.error.NotFoundError
import jsanzo.movies.domain.usecase.GetMovieDetailsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class DetailsViewModelTest {

    private lateinit var detailsViewModel: DetailsViewModel

    @Mock
    private val mockedGetMovieDetailsUseCase = Mockito.mock(GetMovieDetailsUseCase::class.java)

    @Spy
    private lateinit var detailsViewModelStateFlow: StateFlow<DetailsViewState>

    private val movieDetails = MovieDetails(
        adult = false,
        backdropPath = null,
        belongsToCollection = null,
        budget = 0,
        genres = listOf(),
        homepage = null,
        id = 1,
        imdbId = null,
        originalLanguage = EMPTY_STRING,
        originalTitle = EMPTY_STRING,
        overview = null,
        popularity = 0.0,
        posterPath = null,
        productionCompanies = listOf(),
        productionCountries = listOf(),
        releaseDate = EMPTY_STRING,
        revenue = 0,
        runtime = null,
        spokenLanguages = listOf(),
        status = EMPTY_STRING,
        tagline = null,
        title = EMPTY_STRING,
        video = false,
        voteAverage = 0.0,
        voteCount = 0
    )

    private val validMovieId = 0
    private val invalidMovieId = -1

    @Before
    fun setUp() {
        runBlocking {
            `when`(mockedGetMovieDetailsUseCase(invalidMovieId)).thenReturn(NotFoundError.left())
            `when`(mockedGetMovieDetailsUseCase(validMovieId)).thenReturn(movieDetails.right())
        }

        detailsViewModel = DetailsViewModel(mockedGetMovieDetailsUseCase)
        detailsViewModelStateFlow = detailsViewModel.detailsViewModelSateFlow
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `we are always in InitialState at the beginning`() {
        assertTrue(detailsViewModelStateFlow.value is InitialState)
    }

    @Test
    fun `we are in DetailsRetrieved state after call getDetails() with valid id, also we get the movie details`() = runTest {
        detailsViewModel.getDetails(validMovieId)

        verify(mockedGetMovieDetailsUseCase, times(1))(validMovieId)

        assertTrue(detailsViewModelStateFlow.value is DetailsRetrieved)

        val state = detailsViewModelStateFlow.value as? DetailsRetrieved

        assertNotNull(state)
        assertEquals(state?.movieDetails, movieDetails)
    }


    @Test
    fun `we are in ErrorInOperationState state after call getDetails() with invalid id`() = runTest {
        detailsViewModel.getDetails(invalidMovieId)

        verify(mockedGetMovieDetailsUseCase, times(1))(invalidMovieId)

        assertTrue(detailsViewModelStateFlow.value is ErrorInOperation)
    }

}