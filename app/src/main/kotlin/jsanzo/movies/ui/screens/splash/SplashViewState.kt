package jsanzo.movies.ui.screens.splash

sealed class SplashViewState
data object SplashLoading : SplashViewState()
data object MustUpdate : SplashViewState()
data object UpToDate : SplashViewState()
