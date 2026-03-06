package jsanzo.movies.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jsanzo.movies.domain.usecase.MustUpdateUseCase
import jsanzo.movies.domain.utils.onError
import jsanzo.movies.domain.utils.onSuccess
import jsanzo.movies.tracking.MovieTracker
import jsanzo.movies.ui.screens.splash.MustUpdate
import jsanzo.movies.ui.screens.splash.SplashLoading
import jsanzo.movies.ui.screens.splash.SplashViewState
import jsanzo.movies.ui.screens.splash.UpToDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
                    _state.update { MustUpdate }
                } else {
                    _state.update { UpToDate }
                }
            }
            .onError {
                firebaseTracker.trackRemoteConfigError()
                _state.update { UpToDate }
            }
    }
}
