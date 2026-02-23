package jsanzo.movies.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jsanzo.movies.domain.usecase.GetMovieDetailsUseCase
import jsanzo.movies.domain.utils.onError
import jsanzo.movies.domain.utils.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class DetailsViewModel(
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<DetailsViewState>(Loading)
    val state: StateFlow<DetailsViewState> get() = _state

    fun getDetails(movieId: Int) {
        _state.value = Loading

        viewModelScope.launch {
            getMovieDetailsUseCase(movieId)
                .onSuccess { movieDetails ->
                    _state.value = DetailsSuccess(movieDetails)
                }
                .onError { error ->
                    _state.value = DetailsError(error.toString())
                }
        }
    }
}
