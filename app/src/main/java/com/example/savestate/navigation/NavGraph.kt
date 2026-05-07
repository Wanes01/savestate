package com.example.savestate.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.savestate.AppViewModel
import com.example.savestate.ui.screens.auth.AuthScreen
import com.example.savestate.ui.screens.gamedetail.GameDetailsScreen
import com.example.savestate.ui.screens.library.LibraryScreen
import com.example.savestate.ui.screens.profile.ProfileScreen
import com.example.savestate.ui.screens.search.SearchScreen
import com.example.savestate.ui.screens.stats.StatsScreen
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

// application screen destinations
@Serializable
sealed interface NavigationRoute {
    @Serializable
    data object Auth : NavigationRoute
    @Serializable
    data object Library : NavigationRoute
    @Serializable
    data object Search : NavigationRoute
    @Serializable
    data object Stats : NavigationRoute
    @Serializable
    data object Profile : NavigationRoute
    @Serializable
    data class GameDetails(val gameId: Int) : NavigationRoute
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
            LibraryScreen(
                modifier,
                appViewModel,
                onGameClick = { gameId ->
                    navController.navigate(NavigationRoute.GameDetails(gameId))
                }
            )
        }
        composable<NavigationRoute.Search> {
            SearchScreen(
                modifier,
                appViewModel,
                onGameClick = { gameId ->
                    navController.navigate(NavigationRoute.GameDetails(gameId))
                }
            )
        }
        composable<NavigationRoute.GameDetails> { backstackEntry ->
            val route = backstackEntry.toRoute<NavigationRoute.GameDetails>()
            GameDetailsScreen(
                gameId = route.gameId,
                modifier = modifier,
                appViewModel = appViewModel,
                onGoBack = { navController.popBackStack() }
            )
        }
        composable<NavigationRoute.Stats> {
            StatsScreen(modifier, appViewModel)
        }
        composable<NavigationRoute.Profile> {
            ProfileScreen(modifier, appViewModel) {
                appViewModel.logout()
            }
        }
    }
}