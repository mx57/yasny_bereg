package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WellnessDiaryEntity::class,
        PhysiologicalReadingEntity::class,
        MotivationalBookmarkEntity::class,
        CommunityPostEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SobrietyDatabase : RoomDatabase() {
    abstract fun sobrietyDao(): SobrietyDao

    companion object {
        @Volatile
        private var INSTANCE: SobrietyDatabase? = null

        fun getInstance(context: Context): SobrietyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SobrietyDatabase::class.java,
                    "yasny_bereg_encrypted.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
