package jsanzo.movies.ui.compose.navigation

import kotlinx.serialization.Serializable

sealed interface AppDestinations {
    @Serializable
    data object Home : AppDestinations

    @Serializable
    data class Details(val movieId: Int) : AppDestinations
}
