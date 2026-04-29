package com.example.savestate.components
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import com.example.savestate.navigation.NavigationRoute

/**
 * The destinations that are selectable from the bottom navigation bar
 */
enum class BottomNavBarDestination(
    val route: NavigationRoute,
    val icon: ImageVector,
    val label: String
) {
    LIBRARY(NavigationRoute.Library, Icons.Default.GridView, "Library"),
    SEARCH(NavigationRoute.Search, Icons.Default.Search, "Search"),
    STATS(NavigationRoute.Stats, Icons.AutoMirrored.Filled.ShowChart, "Stats"),
    PROFILE(NavigationRoute.Profile, Icons.Default.Person, "Profile")
}

@Composable
fun SavestateBottomNavBar(
    navController: NavHostController
) {
    NavigationBar() {
        BottomNavBarDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = false,
                icon = {
                    Icon(
                        destination.icon,
                        destination.label
                    )
                },
                label = { Text(destination.label) },
                onClick = {
                    navController.navigate(destination.route) {
                        /*
                        Stack's first destination.
                        This is done in order to not accumulate screen routes
                         */
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        // avoids duplicates if the user clicks on the active tab
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}