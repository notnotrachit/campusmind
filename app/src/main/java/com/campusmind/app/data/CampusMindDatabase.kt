package com.campusmind.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
  entities = [
    InboxEntity::class,
    TaskEntity::class,
    FlashcardEntity::class,
    ExpenseEntity::class,
    ActivityLogEntity::class,
    ProcessedNotificationEntity::class,
  ],
  version = 2,
  exportSchema = false,
)
abstract class CampusMindDatabase : RoomDatabase() {
  abstract fun dao(): CampusMindDao

  companion object {
    @Volatile private var instance: CampusMindDatabase? = null

    private val migration1To2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
          """
          CREATE TABLE IF NOT EXISTS processed_notifications (
            notificationKey TEXT NOT NULL PRIMARY KEY,
            processedAtMillis INTEGER NOT NULL
          )
          """.trimIndent(),
        )
      }
    }

    fun get(context: Context): CampusMindDatabase =
      instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
          context.applicationContext,
          CampusMindDatabase::class.java,
          "campusmind.db",
        )
          .addMigrations(migration1To2)
          .build()
          .also { instance = it }
      }
  }
}
