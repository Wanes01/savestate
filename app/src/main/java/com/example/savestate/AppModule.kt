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
import com.example.savestate.data.repositories.FirestoreSyncRepository
import com.example.savestate.data.repositories.LibraryRepository
import com.example.savestate.data.repositories.RawgRepository
import com.example.savestate.data.repositories.StatsRepository
import com.example.savestate.domain.SessionManager
import com.example.savestate.ui.screens.auth.AuthViewModel
import com.example.savestate.ui.screens.gamedetail.GameDetailViewModel
import com.example.savestate.ui.screens.library.LibraryViewModel
import com.example.savestate.ui.screens.profile.ProfileViewModel
import com.example.savestate.ui.screens.search.SearchViewModel
import com.example.savestate.ui.screens.stats.StatsViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore by preferencesDataStore("user_prefs")

val appModule = module {
    // app scope
    single { (androidApplication() as SavestateApplication).applicationScope }

    // datastore and preferences
    single { get<Context>().dataStore }
    single { UserPreferences(get()) }
    single { ThemePreferences(get()) }

    // firebase (this singleton is handled by the firebase SDK)
    single { FirebaseAuth.getInstance() }
    single { Firebase.firestore }
    single { FirestoreSyncRepository(get()) }

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

    // stats
    single { StatsRepository(get(), get(), get()) }

    // daos
    single { get<SavestateDatabase>().userGameDao() }
    single { get<SavestateDatabase>().userAchievementDao() }
    single { get<SavestateDatabase>().gameSessionDao() }

    // active session, requires local context
    single { SessionManager(androidContext(), get(), get()) }

    // repository and viewmodel
    single { AuthRepository(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { AppViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { LibraryViewModel(get(), get()) }
    viewModel { StatsViewModel(get(), get()) }
    // game detail and the profile screen both require local context
    viewModel { GameDetailViewModel(androidApplication(), get(), get(), get(), get()) }
    viewModel { ProfileViewModel(androidApplication(), get()) }
}