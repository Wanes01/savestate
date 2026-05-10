package com.example.savestate

import android.app.Application
import com.example.savestate.data.repositories.AuthRepository
import com.example.savestate.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class SavestateApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        // koin initialization
        startKoin {
            androidContext(this@SavestateApplication)
            modules(appModule)
        }

        // creates notification channels
        NotificationHelper.createChannels(this)

        // synchronizes firebase state with local DataStore on startup
        applicationScope.launch {
            get<AuthRepository>().syncAuthState()
        }
    }
}