package jsanzo.movies.ui.screens.home

import jsanzo.movies.domain.model.DomainMovieResult

sealed class HomeViewState
data object Loading : HomeViewState()
data class MoviesSuccess(val movies: List<DomainMovieResult>) : HomeViewState()
data class MoviesError(val message: String) : HomeViewState()
