package jsanzo.movies.ui.details

import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import jsanzo.movies.common.EMPTY_STRING
import jsanzo.movies.domain.entity.MovieDetails
import jsanzo.movies.domain.error.NotFoundError
import jsanzo.movies.domain.usecase.GetMovieDetailsUseCase
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
class DetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var detailsViewModel: DetailsViewModel

    private val mockedGetMovieDetailsUseCase: GetMovieDetailsUseCase = mockk()

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
        voteCount = 0,
    )

    private val validMovieId = 0
    private val invalidMovieId = -1

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockedGetMovieDetailsUseCase(invalidMovieId) } returns NotFoundError.left()
        coEvery { mockedGetMovieDetailsUseCase(validMovieId) } returns movieDetails.right()

        detailsViewModel = DetailsViewModel(mockedGetMovieDetailsUseCase)
        detailsViewModelStateFlow = detailsViewModel.detailsViewModelSateFlow
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `we are always in InitialState at the beginning`() {
        detailsViewModelStateFlow.value.shouldBeInstanceOf<InitialState>()
    }

    @Test
    fun `we are in DetailsRetrieved state after call getDetails() with valid id, also we get the movie details`() = runTest {
        detailsViewModel.getDetails(validMovieId)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedGetMovieDetailsUseCase(validMovieId) }

        detailsViewModelStateFlow.value.shouldBeInstanceOf<DetailsRetrieved>()

        val state = detailsViewModelStateFlow.value as? DetailsRetrieved

        state?.movieDetails shouldBe movieDetails
    }

    @Test
    fun `we are in ErrorInOperationState state after call getDetails() with invalid id`() = runTest {
        detailsViewModel.getDetails(invalidMovieId)

        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { mockedGetMovieDetailsUseCase(invalidMovieId) }

        detailsViewModelStateFlow.value.shouldBeInstanceOf<ErrorInOperation>()
    }
}
