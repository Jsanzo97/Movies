package jsanzo.movies.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import jsanzo.movies.ui.screens.details.DetailsScreen
import jsanzo.movies.ui.screens.home.HomeScreen

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(AppDestinations.Home)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<AppDestinations.Home> {
                HomeScreen(
                    onNavigateToDetails = { movieId ->
                        backStack.add(AppDestinations.Details(movieId))
                    },
                )
            }
            entry<AppDestinations.Details> { key ->
                DetailsScreen(movieId = key.movieId)
            }
        },
    )
}
