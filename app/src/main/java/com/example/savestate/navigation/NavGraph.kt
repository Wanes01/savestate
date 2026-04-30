package com.example.savestate.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.savestate.ui.theme.screens.auth.AuthScreen
import kotlinx.serialization.Serializable

// application screen destinations
@Serializable sealed interface NavigationRoute {
    @Serializable data object Auth : NavigationRoute
    @Serializable data object Library : NavigationRoute
    @Serializable data object Search : NavigationRoute
    @Serializable data object Stats : NavigationRoute
    @Serializable data object Profile : NavigationRoute
    @Serializable data class GameDetail(val gameId: Int) : NavigationRoute
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoute.Auth,
        enterTransition = {
            fadeIn(tween(300)) + slideInHorizontally(initialOffsetX = { it / 4 })
        },
        exitTransition = {
            fadeOut(tween(300)) + slideOutHorizontally(targetOffsetX = { -it / 4 })
        }
    ) {
        composable<NavigationRoute.Auth> {
            AuthScreen(modifier) { /* cosa fare se il login ha successo */ }
            /*
            // in AuthScreen, dopo login avvenuto con successo
            navController.navigate(NavigationRoute.Library) {
                popUpTo(NavigationRoute.Auth) {
                    inclusive = true  // rimuove anche Auth stessa dallo stack
                }
            }
             */
        }
        composable<NavigationRoute.Library> {
            Text("Libreria utente")
        }
        composable<NavigationRoute.Search> {
            Text("Ricerca")
        }
        composable<NavigationRoute.Stats> {
            Text("Statistiche")
        }
        composable<NavigationRoute.Profile> {
            Text("Profilo e impostazioni")
        }
    }
}