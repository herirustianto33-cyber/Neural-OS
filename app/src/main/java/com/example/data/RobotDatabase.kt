package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RobotStatus::class, VoiceCommandLog::class], version = 3, exportSchema = false)
abstract class RobotDatabase : RoomDatabase() {
    abstract fun robotDao(): RobotDao

    companion object {
        @Volatile
        private var INSTANCE: RobotDatabase? = null

        fun getDatabase(context: Context): RobotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RobotDatabase::class.java,
                    "robot_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
