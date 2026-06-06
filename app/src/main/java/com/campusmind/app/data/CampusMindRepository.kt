package com.campusmind.app.data

import com.campusmind.app.agent.AgentRouter
import com.campusmind.app.model.ActivityLog
import com.campusmind.app.model.AgentResult
import com.campusmind.app.model.InboxItem
import com.campusmind.app.model.InboxType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CampusMindRepository(
  private val dao: CampusMindDao,
  private val router: AgentRouter,
) {
  val inbox: Flow<List<InboxItem>> = dao.observeInbox().map { rows -> rows.map { it.toDomain() } }
  val tasks = dao.observeTasks().map { rows -> rows.map { it.toDomain() } }
  val flashcards = dao.observeFlashcards().map { rows -> rows.map { it.toDomain() } }
  val expenses = dao.observeExpenses().map { rows -> rows.map { it.toDomain() } }
  val logs = dao.observeLogs().map { rows -> rows.map { it.toDomain() } }

  suspend fun submitText(content: String): AgentResult {
    val item = InboxItem(type = InboxType.Text, content = content.trim())
    dao.insertInbox(item.toEntity())
    val result = router.route(item.content)
    persist(result)
    dao.insertLog(
      ActivityLog(
        message = "${result.kind.name} agent processed inbox text with ${result.modelStatusText}",
      ).toEntity(),
    )
    return result
  }

  suspend fun submitMockNotification(content: String): AgentResult {
    val item = InboxItem(type = InboxType.Notification, content = content.trim())
    dao.insertInbox(item.toEntity())
    val result = router.routeNotification(item.content)
    persist(result)
    dao.insertLog(
      ActivityLog(
        message = if (result.hasSavedItems()) {
          "Mock notification saved ${result.savedItemLabel()} with ${result.modelStatusText}"
        } else {
          "Mock notification skipped non-actionable input with ${result.modelStatusText}"
        },
      ).toEntity(),
    )
    return result
  }

  suspend fun submitNotification(notificationKey: String, content: String): AgentResult? {
    val trimmed = content.trim()
    if (trimmed.isBlank()) return null

    val inserted = dao.insertProcessedNotification(ProcessedNotificationEntity(notificationKey))
    if (inserted == -1L) return null

    val item = InboxItem(type = InboxType.Notification, content = trimmed)
    dao.insertInbox(item.toEntity())
    val result = router.routeNotification(item.content)
    persist(result)
    dao.insertLog(
      ActivityLog(
        message = if (result.hasSavedItems()) {
          "Notification agent saved ${result.savedItemLabel()} with ${result.modelStatusText}"
        } else {
          "Notification agent skipped non-actionable notification with ${result.modelStatusText}"
        },
      ).toEntity(),
    )
    return result
  }

  suspend fun deleteTask(taskId: Long) {
    dao.deleteTask(taskId)
    dao.insertLog(ActivityLog(message = "Deleted deadline").toEntity())
  }

  private suspend fun persist(result: AgentResult) {
    if (result.tasks.isNotEmpty()) dao.insertTasks(result.tasks.map { it.toEntity() })
    if (result.flashcards.isNotEmpty()) dao.insertFlashcards(result.flashcards.map { it.toEntity() })
    if (result.expenses.isNotEmpty()) dao.insertExpenses(result.expenses.map { it.toEntity() })
  }

  private fun AgentResult.hasSavedItems(): Boolean =
    tasks.isNotEmpty() || flashcards.isNotEmpty() || expenses.isNotEmpty()

  private fun AgentResult.savedItemLabel(): String =
    listOfNotNull(
      tasks.size.takeIf { it > 0 }?.let { "$it deadline${if (it == 1) "" else "s"}" },
      flashcards.size.takeIf { it > 0 }?.let { "$it flashcard${if (it == 1) "" else "s"}" },
      expenses.size.takeIf { it > 0 }?.let { "$it expense${if (it == 1) "" else "s"}" },
    ).joinToString(", ")
}
