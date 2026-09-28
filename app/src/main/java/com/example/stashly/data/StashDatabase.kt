package com.example.stashly.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [StashItem::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class StashDatabase : RoomDatabase() {

    abstract fun stashDao(): StashDao

    companion object {
        @Volatile
        private var INSTANCE: StashDatabase? = null

        fun getDatabase(context: Context): StashDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StashDatabase::class.java,
                    "stashly_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
