package com.example.savestate.data.database

import androidx.room.TypeConverter
import com.example.savestate.data.models.GameStatus

// converters used because room can't store enums
class SavestateConverters {

    @TypeConverter
    fun fromGameStatus(status: GameStatus): String = status.name

    @TypeConverter
    fun toGameStatus(value: String): GameStatus = GameStatus.valueOf(value)
}