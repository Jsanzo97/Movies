package jsanzo.movies.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jsanzo.movies.domain.entity.MovieResult
import jsanzo.movies.domain.usecase.GetMoviesUseCase
import jsanzo.movies.domain.usecase.SaveMovieUseCase
import jsanzo.movies.domain.utils.onError
import jsanzo.movies.domain.utils.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HomeViewModel(
    private val getMoviesUseCase: GetMoviesUseCase,
    private val saveMovieUseCase: SaveMovieUseCase,
) : ViewModel() {

    private val _homeViewModelStateFlow = MutableStateFlow<HomeViewState>(InitialState)
    val homeViewModelSateFlow: StateFlow<HomeViewState> get() = _homeViewModelStateFlow

    private val threshold = 10
    private val pageSize = 20
    private var nextPageToRetrieve = 1
    private var lastElementRetrieved = 0
    private var lastVisible = 0
    private val moviesRetrieved = mutableListOf<MovieResult>()

    fun getMovies(page: Int = nextPageToRetrieve) {
        viewModelScope.launch {
            if (page == 1) {
                _homeViewModelStateFlow.value = RetrievingMovies
            }

            getMoviesUseCase(page)
                .onSuccess { movies ->
                    movies.results.forEach { movieResult ->
                        if (!moviesRetrieved.contains(movieResult)) {
                            moviesRetrieved.addAll(movies.results)
                        }
                    }
                    _homeViewModelStateFlow.value = MoviesRetrieved(moviesRetrieved)
                }
                .onError { error ->
                    _homeViewModelStateFlow.value = ErrorInOperation(error.toString())
                }
        }
    }

    fun saveMovie(movie: MovieResult) {
        viewModelScope.launch {
            saveMovieUseCase(movie)
                .onSuccess { _homeViewModelStateFlow.value = SavedMovie(movie.id) }
                .onError { _homeViewModelStateFlow.value = ErrorInOperation(it.toString()) }
        }
    }

    fun onStop() {
        _homeViewModelStateFlow.value = InitialState
    }

    private fun checkNeedNewPage() {
        if (lastVisible + threshold >= lastElementRetrieved) {
            lastElementRetrieved += pageSize
            nextPageToRetrieve++
            getMovies()
        }
    }

    fun notifyLastElementVisible(lastElement: Int) {
        if (lastVisible != lastElement) {
            lastVisible = lastElement
            checkNeedNewPage()
        }
    }
}
