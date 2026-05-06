package com.example.savestate

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.example.savestate.data.database.SavestateDatabase
import com.example.savestate.data.datastore.ThemePreferences
import com.example.savestate.data.datastore.UserPreferences
import com.example.savestate.data.network.RawgDataSource
import com.example.savestate.data.network.rawgHttpClient
import com.example.savestate.data.repositories.AuthRepository
import com.example.savestate.data.repositories.LibraryRepository
import com.example.savestate.data.repositories.RawgRepository
import com.example.savestate.ui.theme.screens.auth.AuthViewModel
import com.example.savestate.ui.theme.screens.gamedetail.GameDetailViewModel
import com.example.savestate.ui.theme.screens.library.LibraryViewModel
import com.example.savestate.ui.theme.screens.search.SearchViewModel
import com.google.firebase.auth.FirebaseAuth
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore("user_prefs")

val appModule = module {
    // datastore and preferences
    single { get<Context>().dataStore }
    single { UserPreferences(get()) }
    single { ThemePreferences(get()) }

    // firebase (this singleton is handled by the firebase SDK)
    single { FirebaseAuth.getInstance() }

    // network
    single { rawgHttpClient }
    single { RawgDataSource(get()) }
    single { RawgRepository(get()) }

    // room database and repository
    single {
        Room.databaseBuilder(
            get<Context>(),
            SavestateDatabase::class.java,
            "savestate_database"
        ).build()
    }
    single {
        LibraryRepository(
            get(),
            get(),
            get(),
            get()
        )
    }

    // daos
    single { get<SavestateDatabase>().userGameDao() }
    single { get<SavestateDatabase>().userAchievementDao() }
    single { get<SavestateDatabase>().gameSessionDao() }

    // repository and viewmodel
    single { AuthRepository(get(), get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { AppViewModel(get(), get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { GameDetailViewModel(get(), get()) }
    viewModel { LibraryViewModel(get()) }
}