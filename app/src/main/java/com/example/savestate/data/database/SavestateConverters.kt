package com.example.savestate.data.database

import androidx.room.TypeConverter
import com.example.savestate.data.database.entity.UserGameEntity
import com.example.savestate.data.models.GameStatus
import com.example.savestate.data.models.RawgGameDetail
import com.example.savestate.data.models.RawgGenre
import com.example.savestate.data.models.RawgPlatformWrapper

// converters used because room can't store enums
class SavestateConverters {

    @TypeConverter
    fun fromGameStatus(status: GameStatus): String = status.name

    @TypeConverter
    fun toGameStatus(value: String): GameStatus = GameStatus.valueOf(value)
}