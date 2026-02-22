package jsanzo.movies.ui.compose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                onNavigateToDetails = {
                    navController.navigate(AppDestinations.Details)
                },
            )
        }
        composable<AppDestinations.Details> {
            DetailsScreen()
        }
    }
}
