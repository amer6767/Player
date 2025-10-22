
package com.aiplayer.store

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Experience::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun experienceDao(): ExperienceDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(ctx: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "ai_player_db").build()
                INSTANCE = inst
                inst
            }
        }
    }
}
