package jsanzo.movies.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jsanzo.movies.domain.model.DomainMovie
import jsanzo.movies.domain.usecase.GetLayoutModeUseCase
import jsanzo.movies.domain.usecase.GetMoviesUseCase
import jsanzo.movies.domain.usecase.SaveLayoutModeUseCase
import jsanzo.movies.domain.usecase.SaveMovieUseCase
import jsanzo.movies.domain.usecase.SearchMoviesUseCase
import jsanzo.movies.domain.utils.onError
import jsanzo.movies.domain.utils.onSuccess
import jsanzo.movies.tracking.MovieTracker
import jsanzo.movies.ui.screens.home.HomeViewState
import jsanzo.movies.ui.screens.home.LayoutModeUi
import jsanzo.movies.ui.screens.home.Loading
import jsanzo.movies.ui.screens.home.MovieListComplete
import jsanzo.movies.ui.screens.home.MovieUi
import jsanzo.movies.ui.screens.home.MoviesError
import jsanzo.movies.ui.screens.home.MoviesSearch
import jsanzo.movies.ui.screens.home.toDomainLayoutModePreference
import jsanzo.movies.ui.screens.home.toDomainMovieResult
import jsanzo.movies.ui.screens.home.toMovieUi
import jsanzo.movies.ui.screens.home.toUi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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
    private val getLayoutModeUseCase: GetLayoutModeUseCase,
    private val saveLayoutModeUseCase: SaveLayoutModeUseCase,
    private val firebaseTracker: MovieTracker,
) : ViewModel() {

    private val _state = MutableStateFlow<HomeViewState>(Loading)
    internal val state: StateFlow<HomeViewState> = _state.asStateFlow()

    internal val layoutMode: StateFlow<LayoutModeUi> = getLayoutModeUseCase()
        .map { it.toUi() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = LayoutModeUi.Grid2,
        )

    private val _searchQuery = MutableSharedFlow<String>()

    private var nextPageToRetrieve = 1
    private var lastVisible = 0
    private var isLoadingPage = false
    private val moviesRetrieved = mutableListOf<DomainMovie>()

    init {
        _searchQuery
            .debounce(500L)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isBlank()) {
                    _state.update { MovieListComplete(moviesRetrieved.toMovieUi()) }
                } else {
                    searchMovies(query)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        viewModelScope.launch {
            _searchQuery.emit(query)
        }
    }

    fun getMovies(page: Int = nextPageToRetrieve) {
        if (!isLoadingPage) {
            viewModelScope.launch {
                isLoadingPage = true
                if (page == 1) _state.update { Loading }
                getMoviesUseCase(page)
                    .onSuccess { movies ->
                        val existingIds = moviesRetrieved.map { it.id }.toSet()
                        moviesRetrieved.addAll(movies.filter { it.id !in existingIds })
                        nextPageToRetrieve++
                        firebaseTracker.trackPageLoaded(page)
                        _state.update { MovieListComplete(moviesRetrieved.toMovieUi()) }
                    }
                    .onError { error ->
                        firebaseTracker.trackErrorShown("home", error.toString())
                        _state.update { MoviesError(error.toString()) }
                    }
                isLoadingPage = false
            }
        }
    }

    private fun searchMovies(query: String) {
        viewModelScope.launch {
            _state.update { Loading }
            searchMoviesUseCase(query)
                .onSuccess { movies ->
                    firebaseTracker.trackSearchPerformed(query, movies.size)
                    _state.update { MoviesSearch(movies.toMovieUi()) }
                }
                .onError { error ->
                    firebaseTracker.trackErrorShown("home_search", error.toString())
                    _state.update { MoviesError(error.toString()) }
                }
        }
    }

    fun saveMovie(movie: MovieUi) {
        viewModelScope.launch {
            firebaseTracker.trackMovieClicked(movie.id, movie.title)
            saveMovieUseCase(movie.toDomainMovieResult())
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
        if (_state.value is MovieListComplete && lastVisible + PAGINATION_THRESHOLD >= moviesRetrieved.size) {
            getMovies()
        }
    }

    internal fun saveLayoutMode(mode: LayoutModeUi) {
        firebaseTracker.trackLayoutModeChanged(mode.name)
        viewModelScope.launch { saveLayoutModeUseCase(mode.toDomainLayoutModePreference()) }
    }
}
