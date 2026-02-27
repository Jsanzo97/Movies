package jsanzo.movies.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AppDestinations : NavKey {

    @Serializable data object Splash : AppDestinations

    @Serializable
    data object Home : AppDestinations

    @Serializable
    data class Details(val movieId: Int) : AppDestinations
}
