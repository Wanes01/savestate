package com.example.savestate.data.database

import com.example.savestate.data.database.entity.UserGameEntity
import com.example.savestate.data.database.entity.UserAchievementEntity
import com.example.savestate.data.database.entity.GameSessionEntity
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.savestate.data.database.dao.GameSessionDao
import com.example.savestate.data.database.dao.UserAchievementDao
import com.example.savestate.data.database.dao.UserGameDao

@Database(
    entities = [
        UserGameEntity::class,
        UserAchievementEntity::class,
        GameSessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(SavestateConverters::class)
abstract class SavestateDatabase : RoomDatabase() {
    abstract fun userGameDao(): UserGameDao
    abstract fun userAchievementDao(): UserAchievementDao
    abstract fun gameSessionDao(): GameSessionDao
}