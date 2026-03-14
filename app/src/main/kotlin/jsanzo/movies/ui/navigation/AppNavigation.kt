package jsanzo.movies.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import jsanzo.movies.ui.screens.details.DetailsScreen
import jsanzo.movies.ui.screens.home.HomeScreen
import jsanzo.movies.ui.screens.splash.SplashScreen
import jsanzo.movies.ui.screens.update.ForceUpdateScreen

@Suppress("LongMethod")
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(AppDestinations.Splash as AppDestinations)
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            val directionMultiplier = if (isRtl) -1 else 1
            val destination = targetState.key
            when (destination) {
                is AppDestinations.Home, is AppDestinations.ForceUpdate -> {
                    fadeIn(
                        animationSpec = tween(300),
                    ) togetherWith fadeOut(
                        animationSpec = tween(500),
                    )
                }

                else -> {
                    slideInHorizontally(
                        initialOffsetX = { it * directionMultiplier },
                        animationSpec = tween(300),
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it * directionMultiplier },
                        animationSpec = tween(300),
                    )
                }
            }
        },
        popTransitionSpec = {
            val directionMultiplier = if (isRtl) -1 else 1
            slideInHorizontally(
                initialOffsetX = { -it * directionMultiplier },
                animationSpec = tween(300),
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it * directionMultiplier },
                animationSpec = tween(300),
            )
        },
        predictivePopTransitionSpec = {
            val directionMultiplier = if (isRtl) -1 else 1
            slideInHorizontally(
                initialOffsetX = { -it * directionMultiplier },
                animationSpec = tween(300),
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it * directionMultiplier },
                animationSpec = tween(300),
            )
        },
        entryProvider = entryProvider {
            entry<AppDestinations.Splash> {
                SplashScreen(
                    onNavigateToHome = {
                        backStack.removeLastOrNull()
                        backStack.add(AppDestinations.Home)
                    },
                    onNavigateToForceUpdate = {
                        backStack.removeLastOrNull()
                        backStack.add(AppDestinations.ForceUpdate)
                    },
                )
            }
            entry<AppDestinations.ForceUpdate> {
                ForceUpdateScreen()
            }
            entry<AppDestinations.Home> {
                HomeScreen(
                    windowInsets = getWindowNavigationInsets(),
                    onNavigateToDetails = { movieId ->
                        backStack.add(AppDestinations.Details(movieId))
                    },
                )
            }
            entry<AppDestinations.Details> { key ->
                DetailsScreen(
                    windowInsets = getWindowNavigationInsets(),
                    movieId = key.movieId,
                )
            }
        },
    )
}

@Composable
fun getWindowNavigationInsets(): WindowInsets {
    val view = LocalView.current
    val insets = ViewCompat.getRootWindowInsets(view) ?: return WindowInsets.safeDrawing

    val gestureInsets = insets.getInsets(WindowInsetsCompat.Type.systemGestures())

    return if (gestureInsets.left > 0 || gestureInsets.right > 0) {
        WindowInsets.statusBars
    } else {
        WindowInsets.safeDrawing
    }
}
