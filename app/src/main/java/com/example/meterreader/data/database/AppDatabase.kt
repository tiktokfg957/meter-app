package com.example.meterreader.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.meterreader.data.model.*

@Database(
    entities = [
        User::class,
        MeterData::class,
        ReadingData::class,
        GoalData::class
    ],
    version = 1,  // если раньше было 2, можно оставить 2, но лучше вернуть на 1, если нет миграций
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun meterDao(): MeterDao
    abstract fun readingDao(): ReadingDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meter_reader_db"
                ).fallbackToDestructiveMigration()  // на случай изменения версии
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
