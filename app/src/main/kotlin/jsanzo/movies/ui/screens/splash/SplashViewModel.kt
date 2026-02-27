package jsanzo.movies.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jsanzo.movies.domain.usecase.MustUpdateUseCase
import jsanzo.movies.domain.utils.onError
import jsanzo.movies.domain.utils.onSuccess
import jsanzo.movies.tracking.MovieTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SplashViewModel(
    private val mustUpdateUseCase: MustUpdateUseCase,
    private val firebaseTracker: MovieTracker,
) : ViewModel() {

    private val _state = MutableStateFlow<SplashViewState>(SplashLoading)
    val state: StateFlow<SplashViewState> get() = _state

    fun trackScreenView() {
        firebaseTracker.trackSplashShown()
    }

    fun mustUpdate(actualVersion: String) = viewModelScope.launch {
        mustUpdateUseCase(actualVersion)
            .onSuccess { mustUpdate ->
                if (mustUpdate) {
                    firebaseTracker.trackForceUpdateShown(actualVersion)
                    _state.value = MustUpdate
                } else {
                    _state.value = UpToDate
                }
            }
            .onError {
                firebaseTracker.trackRemoteConfigError()
                _state.value = UpToDate
            }
    }
}
