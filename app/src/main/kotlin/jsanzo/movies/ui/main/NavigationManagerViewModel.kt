package jsanzo.movies.ui.main

import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavGraph
import jsanzo.movies.ui.home.HomeFragmentDirections

class NavigationManagerViewModel: ViewModel() {

    fun navigateToDetails(navController: NavController, movieId: Int) {
        val direction = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(movieId)
        safeNavigation(navController, direction)
    }

    private fun safeNavigation(navController: NavController, direction: NavDirections) {
        val currentDestination = navController.currentDestination

        val destinationId = currentDestination?.getAction(direction.actionId)?.destinationId ?: 0

        currentDestination?.let { node ->
            val currentNode = when (node) {
                is NavGraph -> node
                else -> node.parent
            }

            if (destinationId != 0) {
                currentNode?.findNode(destinationId)?.let {
                    navController.navigate(direction)
                }
            }
        }
    }
}