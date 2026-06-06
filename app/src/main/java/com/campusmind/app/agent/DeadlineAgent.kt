package com.campusmind.app.agent

import com.campusmind.app.ai.RuntimeOrchestrator
import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.AgentResult
import com.campusmind.app.model.TaskItem

class DeadlineAgent(
  private val runtimeOrchestrator: RuntimeOrchestrator? = null,
) : StudentAgent {
  override val kind = AgentKind.Deadline

  override suspend fun analyze(inputText: String): AgentResult {
    var fallbackStatus = "Deterministic fallback"
    runtimeOrchestrator?.generate(deadlinePrompt(inputText))?.getOrNull()?.let { generation ->
      AgentJsonParser.parse(kind, generation.text, inputText, generation.runtimeType, generation.statusText)?.let { return it }
      fallbackStatus = "Deterministic fallback after malformed LLM response from ${generation.runtimeType.name}"
    }

    val dueText = extractDueText(inputText)
    val title = inputText
      .lineSequence()
      .firstOrNull { it.isNotBlank() }
      ?.take(72)
      ?: "Student deadline"

    return AgentResult(
      kind = kind,
      summary = "Created one deadline from the inbox item.",
      runtimeStatusText = fallbackStatus,
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

  private fun deadlinePrompt(inputText: String): String =
    """
    You are CampusMind's Deadline agent. Convert the student message into JSON only.
    Schema: {"summary":"short summary","tasks":[{"title":"task title","dueDateText":"due date phrase"}]}
    Create one task. Use "Review soon" when no due date is present. Do not include markdown.
    Input:
    $inputText
    """.trimIndent()
}
