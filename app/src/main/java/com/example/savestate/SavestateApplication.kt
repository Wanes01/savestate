package com.example.savestate

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

// koin initialization
class SavestateApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SavestateApplication)
            modules(appModule)
        }
    }
}