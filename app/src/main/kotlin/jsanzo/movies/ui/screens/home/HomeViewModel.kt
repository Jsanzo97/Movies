package jsanzo.movies.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jsanzo.movies.domain.model.DomainMovieResult
import jsanzo.movies.domain.usecase.GetMoviesUseCase
import jsanzo.movies.domain.usecase.SaveMovieUseCase
import jsanzo.movies.domain.utils.onError
import jsanzo.movies.domain.utils.onSuccess
import jsanzo.movies.tracking.MovieTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel(
    private val getMoviesUseCase: GetMoviesUseCase,
    private val saveMovieUseCase: SaveMovieUseCase,
    private val firebaseTracker: MovieTracker,
) : ViewModel() {

    private val _state = MutableStateFlow<HomeViewState>(Loading)
    val state: StateFlow<HomeViewState> get() = _state

    private val threshold = 10
    private var nextPageToRetrieve = 1
    private var lastVisible = 0
    private val moviesRetrieved = mutableListOf<DomainMovieResult>()
    private var loadingJob: Job? = null

    fun getMovies(page: Int = nextPageToRetrieve) {
        if (loadingJob?.isActive == true) return
        loadingJob = viewModelScope.launch {
            if (page == 1) {
                _state.update { Loading }
            }

            getMoviesUseCase(page)
                .onSuccess { movies ->
                    val existingIds = moviesRetrieved.map { it.id }.toSet()
                    val newMovies = movies.results.filter { it.id !in existingIds }
                    moviesRetrieved.addAll(newMovies)
                    nextPageToRetrieve++
                    firebaseTracker.trackPageLoaded(page)
                    _state.update { MoviesSuccess(moviesRetrieved.toList()) }
                }
                .onError { error ->
                    firebaseTracker.trackErrorShown("home", error.toString())
                    _state.update { MoviesError(error.toString()) }
                }
        }
    }

    fun saveMovie(movie: DomainMovieResult) {
        viewModelScope.launch {
            firebaseTracker.trackMovieClicked(movie.id, movie.title)
            saveMovieUseCase(movie)
        }
    }

    fun trackScreenView() {
        firebaseTracker.trackHomeShown()
    }

    fun notifyLastElementVisible(lastElement: Int) {
        if (lastVisible != lastElement) {
            lastVisible = lastElement
            checkNeedNewPage()
        }
    }

    private fun checkNeedNewPage() {
        val totalLoaded = moviesRetrieved.size
        if (lastVisible + threshold >= totalLoaded) {
            getMovies()
        }
    }
}
