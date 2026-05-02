package com.example.savestate.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.savestate.AppViewModel
import com.example.savestate.ui.theme.screens.auth.AuthScreen
import com.example.savestate.ui.theme.screens.auth.AuthViewModel
import com.example.savestate.ui.theme.screens.profile.ProfileScreen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

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
    val appViewModel: AppViewModel = koinViewModel()
    val userData by appViewModel.userData.collectAsStateWithLifecycle()
    val isReady by appViewModel.isReady.collectAsStateWithLifecycle()

    // it's still reading the data store
    if (!isReady) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (userData.isLoggedIn) {
        NavigationRoute.Library
    } else {
        NavigationRoute.Auth
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(tween(300)) + slideInHorizontally(initialOffsetX = { it / 4 })
        },
        exitTransition = {
            fadeOut(tween(300)) + slideOutHorizontally(targetOffsetX = { -it / 4 })
        }
    ) {
        composable<NavigationRoute.Auth> {
            AuthScreen(modifier, wasLoginSuccessful = userData.isLoggedIn) {
                navController.navigate(NavigationRoute.Library) {
                    popUpTo(NavigationRoute.Auth) {
                        inclusive = true
                    }
                }
            }
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
            ProfileScreen(modifier, appViewModel)
        }
    }
}