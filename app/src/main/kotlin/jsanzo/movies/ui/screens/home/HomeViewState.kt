package jsanzo.movies.ui.screens.home

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList

@Stable
sealed class HomeViewState

@Immutable
data object Loading : HomeViewState()

@Immutable
data class MovieListComplete(val movies: ImmutableList<MovieUi>) : HomeViewState()

@Immutable
data class MoviesSearch(val movies: ImmutableList<MovieUi>) : HomeViewState()

@Immutable
data class MoviesError(val message: String) : HomeViewState()
