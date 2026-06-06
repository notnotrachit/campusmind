package com.campusmind.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.campusmind.app.model.ActivityLog
import com.campusmind.app.model.ExpenseItem
import com.campusmind.app.model.Flashcard
import com.campusmind.app.model.InboxItem
import com.campusmind.app.model.InboxType
import com.campusmind.app.model.TaskItem

@Entity(tableName = "inbox_items")
data class InboxEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val type: String,
  val content: String,
  val createdAtMillis: Long,
)

@Entity(tableName = "tasks")
data class TaskEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val dueDateText: String,
  val source: String,
  val done: Boolean,
)

@Entity(tableName = "flashcards")
data class FlashcardEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val front: String,
  val back: String,
  val source: String,
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val amountText: String,
  val category: String,
  val merchant: String,
  val source: String,
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val message: String,
  val createdAtMillis: Long,
)

@Entity(tableName = "processed_notifications")
data class ProcessedNotificationEntity(
  @PrimaryKey val notificationKey: String,
  val processedAtMillis: Long = System.currentTimeMillis(),
)

fun InboxEntity.toDomain() =
  InboxItem(
    id = id,
    type = InboxType.valueOf(type),
    content = content,
    createdAtMillis = createdAtMillis,
  )

fun InboxItem.toEntity() =
  InboxEntity(
    id = id,
    type = type.name,
    content = content,
    createdAtMillis = createdAtMillis,
  )

fun TaskEntity.toDomain() = TaskItem(id, title, dueDateText, source, done)
fun TaskItem.toEntity() = TaskEntity(id, title, dueDateText, source, done)

fun FlashcardEntity.toDomain() = Flashcard(id, front, back, source)
fun Flashcard.toEntity() = FlashcardEntity(id, front, back, source)

fun ExpenseEntity.toDomain() = ExpenseItem(id, amountText, category, merchant, source)
fun ExpenseItem.toEntity() = ExpenseEntity(id, amountText, category, merchant, source)

fun ActivityLogEntity.toDomain() = ActivityLog(id, message, createdAtMillis)
fun ActivityLog.toEntity() = ActivityLogEntity(id, message, createdAtMillis)
