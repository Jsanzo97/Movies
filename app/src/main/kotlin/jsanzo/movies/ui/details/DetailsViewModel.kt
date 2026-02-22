package jsanzo.movies.ui.details

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

    private val _detailsViewModelStateFlow = MutableStateFlow<DetailsViewState>(InitialState)
    val detailsViewModelSateFlow: StateFlow<DetailsViewState> get() = _detailsViewModelStateFlow

    fun getDetails(movieId: Int) {
        _detailsViewModelStateFlow.value = RetrievingDetails

        viewModelScope.launch {
            getMovieDetailsUseCase(movieId)
                .onSuccess { movieDetails ->
                    _detailsViewModelStateFlow.value = DetailsRetrieved(movieDetails)
                }
                .onError { error ->
                    _detailsViewModelStateFlow.value = ErrorInOperation(error.toString())
                }
        }
    }
}
