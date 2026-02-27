package jsanzo.movies.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import jsanzo.movies.ui.screens.SplashScreen
import jsanzo.movies.ui.screens.details.DetailsScreen
import jsanzo.movies.ui.screens.home.HomeScreen

@Composable
fun AppNavigation(onSplashFinished: () -> Unit = {}) {
    val backStack = rememberNavBackStack(AppDestinations.Splash)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300),
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300),
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300),
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300),
            )
        },
        entryProvider = entryProvider {
            entry<AppDestinations.Splash> {
                SplashScreen(
                    onAnimationFinished = {
                        onSplashFinished()
                        backStack.removeLastOrNull()
                        backStack.add(AppDestinations.Home)
                    },
                )
            }
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
