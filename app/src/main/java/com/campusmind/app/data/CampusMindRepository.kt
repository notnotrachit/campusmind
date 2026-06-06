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

  private suspend fun persist(result: AgentResult) {
    if (result.tasks.isNotEmpty()) dao.insertTasks(result.tasks.map { it.toEntity() })
    if (result.flashcards.isNotEmpty()) dao.insertFlashcards(result.flashcards.map { it.toEntity() })
    if (result.expenses.isNotEmpty()) dao.insertExpenses(result.expenses.map { it.toEntity() })
  }
}
