package jsanzo.movies.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jsanzo.movies.domain.usecase.GetMovieDetailsUseCase
import jsanzo.movies.domain.utils.onError
import jsanzo.movies.domain.utils.onSuccess
import jsanzo.movies.tracking.MovieTracker
import jsanzo.movies.ui.screens.details.DetailsError
import jsanzo.movies.ui.screens.details.DetailsSuccess
import jsanzo.movies.ui.screens.details.DetailsViewState
import jsanzo.movies.ui.screens.details.Loading
import jsanzo.movies.ui.screens.details.toMovieDetailsUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class DetailsViewModel(
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
    private val firebaseTracker: MovieTracker,
) : ViewModel() {

    private val _state = MutableStateFlow<DetailsViewState>(Loading)
    val state: StateFlow<DetailsViewState> get() = _state

    fun getDetails(movieId: Int) {
        _state.value = Loading
        viewModelScope.launch {
            getMovieDetailsUseCase(movieId)
                .onSuccess { movieDetails ->
                    _state.value = DetailsSuccess(movieDetails.toMovieDetailsUi())
                }
                .onError { error ->
                    firebaseTracker.trackErrorShown("details", error.toString())
                    _state.value = DetailsError(error.toString())
                }
        }
    }

    fun trackScreenView(movieId: Int) {
        firebaseTracker.trackDetailsShown(movieId)
    }
}
