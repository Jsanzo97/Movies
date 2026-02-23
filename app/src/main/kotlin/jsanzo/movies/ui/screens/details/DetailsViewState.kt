package jsanzo.movies.ui.screens.details

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import jsanzo.movies.domain.model.DomainMovieDetails

@Stable
sealed class DetailsViewState

data object Loading : DetailsViewState()

@Immutable
data class DetailsSuccess(val movieDetails: DomainMovieDetails) : DetailsViewState()

@Immutable
data class DetailsError(val message: String) : DetailsViewState()
