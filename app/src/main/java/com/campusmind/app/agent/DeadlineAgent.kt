package com.campusmind.app.agent

import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.AgentResult
import com.campusmind.app.model.TaskItem

class DeadlineAgent : StudentAgent {
  override val kind = AgentKind.Deadline

  override suspend fun analyze(inputText: String): AgentResult {
    val dueText = extractDueText(inputText)
    val title = inputText
      .lineSequence()
      .firstOrNull { it.isNotBlank() }
      ?.take(72)
      ?: "Student deadline"

    return AgentResult(
      kind = kind,
      summary = "Created one deadline from the inbox item.",
      tasks = listOf(
        TaskItem(
          title = title,
          dueDateText = dueText,
          source = inputText.take(140),
        ),
      ),
    )
  }

  private fun extractDueText(inputText: String): String {
    val lower = inputText.lowercase()
    val keywords = listOf("today", "tomorrow", "friday", "monday", "deadline", "submit", "due")
    return keywords.firstOrNull { lower.contains(it) }?.replaceFirstChar { it.uppercase() }
      ?: "Review soon"
  }
}
