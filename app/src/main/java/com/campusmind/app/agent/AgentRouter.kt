package com.campusmind.app.agent

import com.campusmind.app.ai.SingleModelRunner
import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.AgentResult
import com.campusmind.app.model.CAMPUS_MODEL_STATUS

class AgentRouter(
  private val modelRunner: SingleModelRunner,
  private val studyAgent: StudentAgent = StudyAgent(modelRunner),
  private val deadlineAgent: StudentAgent = DeadlineAgent(modelRunner),
  private val expenseAgent: StudentAgent = ExpenseAgent(modelRunner),
) {
  suspend fun route(inputText: String): AgentResult {
    AgentFallbacks.structured(inputText)?.let { return it }

    val kind = chooseKind(inputText)
    return when (kind) {
      AgentKind.Study -> studyAgent.analyze(inputText)
      AgentKind.Deadline -> deadlineAgent.analyze(inputText)
      AgentKind.Expense -> expenseAgent.analyze(inputText)
      AgentKind.Notification -> routeNotification(inputText)
    }
  }

  suspend fun routeNotification(inputText: String): AgentResult {
    return modelRunner.generateStructured(notificationPrompt(inputText), AgentSchemas.notification)
      .fold(
        onSuccess = { text ->
          AgentJsonParser.parse(AgentKind.Notification, text, inputText, CAMPUS_MODEL_STATUS)
            ?: notificationFallback(inputText)
        },
        onFailure = { notificationFallback(inputText) },
      )
  }

  fun chooseKind(inputText: String): AgentKind {
    val lower = inputText.lowercase()
    return when {
      listOf("rs", "inr", "₹", "upi", "paid", "receipt", "canteen", "total").any(lower::contains) -> AgentKind.Expense
      listOf("submit", "deadline", "due", "assignment", "exam", "remind").any(lower::contains) -> AgentKind.Deadline
      else -> AgentKind.Study
    }
  }

  private fun notificationPrompt(inputText: String): String =
    """
    Classify this phone notification for a student. Extract every actionable item into structured arrays.
    Only create tasks for real deadlines or student actions. Create flashcards for study concepts.
    Create expenses for payments or receipts. If it is not useful for student productivity, set important=false
    and return empty tasks, flashcards, and expenses.

    Notification:
    $inputText
    """.trimIndent()

  private fun notificationFallback(inputText: String): AgentResult =
    AgentFallbacks.structured(inputText)?.copy(kind = AgentKind.Notification)
      ?: AgentResult(
        kind = AgentKind.Notification,
        summary = "Ignored non-actionable notification",
        modelStatusText = "Local fallback",
      )
}
