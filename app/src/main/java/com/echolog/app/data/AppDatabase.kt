package com.echolog.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.echolog.app.data.Converters
import com.echolog.app.data.LogDao
import com.echolog.app.data.LogEntity


@Database(entities = [LogEntity::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
}