package jsanzo.movies.ui.screens.details

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
sealed class DetailsViewState

@Immutable
data object Loading : DetailsViewState()

@Immutable
data class DetailsSuccess(val movieDetails: MovieDetailsUi) : DetailsViewState()

@Immutable
data class DetailsError(val message: String) : DetailsViewState()
