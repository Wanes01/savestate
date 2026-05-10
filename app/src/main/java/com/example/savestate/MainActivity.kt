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
import androidx.navigation.compose.rememberNavController
import com.example.savestate.navigation.NavGraph
import com.example.savestate.ui.components.SavestateBottomNavBar
import com.example.savestate.ui.components.SavestateTopBar
import com.example.savestate.ui.components.SyncingScreen
import com.example.savestate.ui.theme.SavestateTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = koinViewModel()
            val theme by appViewModel.theme.collectAsStateWithLifecycle()
            val isSyncing by appViewModel.isSyncing.collectAsStateWithLifecycle()

            SavestateTheme(theme) {
                // sets up the navigation system
                val navController = rememberNavController()
                val userData by appViewModel.userData.collectAsStateWithLifecycle()
                val topBarState by appViewModel.topBarState.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (userData.isLoggedIn && topBarState.isTopBarVisible) {
                            SavestateTopBar(
                                title = topBarState.title,
                                actions = topBarState.actions ?: {}
                            )
                        }
                    },
                    bottomBar = {
                        if (userData.isLoggedIn) SavestateBottomNavBar(navController)
                    },
                ) { innerPadding ->
                    if (isSyncing) {
                        SyncingScreen()
                    } else {
                        NavGraph(
                            navController = navController,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}