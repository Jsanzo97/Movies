package jsanzo.movies.ui.compose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import jsanzo.movies.ui.compose.screens.details.DetailsScreen
import jsanzo.movies.ui.compose.screens.home.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppDestinations.Home,
    ) {
        composable<AppDestinations.Home> {
            HomeScreen(
                onNavigateToDetails = { movieId ->
                    navController.navigate(AppDestinations.Details(movieId))
                },
            )
        }
        composable<AppDestinations.Details> { backStackEntry ->
            val destination = backStackEntry.toRoute<AppDestinations.Details>()
            DetailsScreen(movieId = destination.movieId)
        }
    }
}
