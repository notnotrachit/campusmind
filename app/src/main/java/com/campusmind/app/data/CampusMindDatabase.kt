package com.campusmind.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    InboxEntity::class,
    TaskEntity::class,
    FlashcardEntity::class,
    ExpenseEntity::class,
    ActivityLogEntity::class,
  ],
  version = 1,
  exportSchema = false,
)
abstract class CampusMindDatabase : RoomDatabase() {
  abstract fun dao(): CampusMindDao

  companion object {
    @Volatile private var instance: CampusMindDatabase? = null

    fun get(context: Context): CampusMindDatabase =
      instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
          context.applicationContext,
          CampusMindDatabase::class.java,
          "campusmind.db",
        ).build().also { instance = it }
      }
  }
}
