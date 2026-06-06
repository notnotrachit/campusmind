package com.campusmind.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CampusMindDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInbox(item: InboxEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTasks(items: List<TaskEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFlashcards(items: List<FlashcardEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpenses(items: List<ExpenseEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLog(item: ActivityLogEntity)

  @Query("SELECT * FROM inbox_items ORDER BY createdAtMillis DESC LIMIT 20")
  fun observeInbox(): Flow<List<InboxEntity>>

  @Query("SELECT * FROM tasks ORDER BY id DESC LIMIT 20")
  fun observeTasks(): Flow<List<TaskEntity>>

  @Query("SELECT * FROM flashcards ORDER BY id DESC LIMIT 20")
  fun observeFlashcards(): Flow<List<FlashcardEntity>>

  @Query("SELECT * FROM expenses ORDER BY id DESC LIMIT 20")
  fun observeExpenses(): Flow<List<ExpenseEntity>>

  @Query("SELECT * FROM activity_logs ORDER BY createdAtMillis DESC LIMIT 30")
  fun observeLogs(): Flow<List<ActivityLogEntity>>
}
