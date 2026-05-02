package com.example.savestate

import android.app.Application
import com.example.savestate.data.repositories.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class SavestateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // koin initialization
        startKoin {
            androidContext(this@SavestateApplication)
            modules(appModule)
        }

        // synchronizes firebase state with local DataStore on startup
        CoroutineScope(Dispatchers.IO).launch {
            get<AuthRepository>().syncAuthState()
        }
    }
}