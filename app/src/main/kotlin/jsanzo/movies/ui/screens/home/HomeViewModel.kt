package jsanzo.movies.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jsanzo.movies.domain.model.DomainMovieResult
import jsanzo.movies.domain.usecase.GetMoviesUseCase
import jsanzo.movies.domain.usecase.SaveMovieUseCase
import jsanzo.movies.domain.usecase.SearchMoviesUseCase
import jsanzo.movies.domain.utils.onError
import jsanzo.movies.domain.utils.onSuccess
import jsanzo.movies.tracking.MovieTracker
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

private const val PAGINATION_THRESHOLD = 10

@OptIn(FlowPreview::class)
@KoinViewModel
class HomeViewModel(
    private val getMoviesUseCase: GetMoviesUseCase,
    private val saveMovieUseCase: SaveMovieUseCase,
    private val searchMoviesUseCase: SearchMoviesUseCase,
    private val firebaseTracker: MovieTracker,
) : ViewModel() {

    private val _state = MutableStateFlow<HomeViewState>(Loading)
    val state: StateFlow<HomeViewState> get() = _state

    private val _searchQuery = MutableStateFlow("")

    private var nextPageToRetrieve = 1
    private var lastVisible = 0
    private val moviesRetrieved = mutableListOf<DomainMovieResult>()
    private var currentJob: Job? = null

    init {
        _searchQuery
            .debounce(500L)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isBlank() && moviesRetrieved.isNotEmpty()) {
                    _state.update { MoviesSuccess(moviesRetrieved.toList()) }
                } else if (query.isNotBlank()) {
                    searchMovies(query)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.update { query }
    }

    fun getMovies(page: Int = nextPageToRetrieve) {
        if (currentJob?.isActive == true) return
        currentJob = viewModelScope.launch {
            if (page == 1) _state.update { Loading }

            getMoviesUseCase(page)
                .onSuccess { movies ->
                    val existingIds = moviesRetrieved.map { it.id }.toSet()
                    moviesRetrieved.addAll(movies.results.filter { it.id !in existingIds })
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

    private fun searchMovies(query: String) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            _state.update { Loading }
            searchMoviesUseCase(query)
                .onSuccess { movies ->
                    firebaseTracker.trackSearchPerformed(query, movies.results.size)
                    _state.update { MoviesSuccess(movies.results) }
                }
                .onError { error ->
                    firebaseTracker.trackErrorShown("home_search", error.toString())
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
        if (_searchQuery.value.isBlank() && lastVisible + PAGINATION_THRESHOLD >= moviesRetrieved.size) {
            getMovies()
        }
    }
}
