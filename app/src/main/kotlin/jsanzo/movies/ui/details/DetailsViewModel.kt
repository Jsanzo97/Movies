package jsanzo.movies.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jsanzo.movies.domain.usecase.GetMovieDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailsViewModel(
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
) : ViewModel() {

    private val _detailsViewModelStateFlow = MutableStateFlow<DetailsViewState>(InitialState)
    val detailsViewModelSateFlow: StateFlow<DetailsViewState> get() = _detailsViewModelStateFlow

    fun getDetails(movieId: Int) {
        _detailsViewModelStateFlow.value = RetrievingDetails

        viewModelScope.launch {
            getMovieDetailsUseCase(movieId).fold(
                ifLeft = { error ->
                    _detailsViewModelStateFlow.value = ErrorInOperation(error.toString())
                },
                ifRight = { movieDetails ->
                    _detailsViewModelStateFlow.value = DetailsRetrieved(movieDetails)
                },
            )
        }
    }
}
