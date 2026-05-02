package com.example.savestate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.savestate.ui.theme.components.SavestateBottomNavBar
import com.example.savestate.navigation.NavGraph
import com.example.savestate.navigation.NavigationRoute
import com.example.savestate.ui.theme.SavestateTheme
import com.example.savestate.ui.theme.components.BottomNavBarDestination
import com.example.savestate.ui.theme.components.SavestateTopBar
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SavestateTheme {
                // sets up the navigation system
                val navController = rememberNavController()
                val appViewModel: AppViewModel = koinViewModel()
                val topBarState by appViewModel.topBarState.collectAsStateWithLifecycle()

                // gets the current route. This is done to
                // hide the nav and top bars in the auth screen
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route
                val showBars = currentRoute != NavigationRoute.Auth::class.qualifiedName

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (showBars) {
                            SavestateTopBar(
                                title = topBarState.title,
                                actions = topBarState.actions ?: {}
                            )
                        }
                    },
                    bottomBar = {
                        if (showBars) SavestateBottomNavBar(navController)
                    },
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}