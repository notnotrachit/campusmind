package com.campusmind.app.agent

import com.campusmind.app.ai.SingleModelRunner
import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.AgentResult
import com.campusmind.app.model.CAMPUS_MODEL_STATUS
import com.campusmind.app.model.TaskItem
import com.campusmind.app.model.TaskPrioritySuggestion
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    AgentFallbacks.structured(inputText)?.let { return it.asNotificationResult() }

    return modelRunner.generate(notificationPrompt(inputText))
      .fold(
        onSuccess = { text ->
          AgentJsonParser.parse(AgentKind.Notification, text, inputText, CAMPUS_MODEL_STATUS)
            ?: notificationFallbackResult(inputText, "Model returned malformed notification JSON")
        },
        onFailure = { error ->
          notificationFallbackResult(
            inputText,
            "Notification model failed: ${error.message ?: error::class.java.simpleName}",
          )
        },
      )
  }

  suspend fun prioritizeTasks(tasks: List<TaskItem>): Result<List<TaskPrioritySuggestion>> {
    val openTasks = tasks.filterNot { it.done }.take(8)
    if (openTasks.isEmpty()) return Result.success(emptyList())

    return Result.success(localNextActions(openTasks))
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
    Return only a JSON object with keys: important, category, summary, tasks, flashcards, expenses.
    Each task must have title and dueDateText. Each flashcard must have front and back.
    Each expense must have amountText, category, and merchant.
    Only create tasks for real deadlines or student actions. Create flashcards for study concepts.
    Create expenses for payments or receipts. If it is not useful for student productivity, set important=false
    and return empty tasks, flashcards, and expenses.
    Current date/time for resolving relative due dates: ${currentDateTimeContext()}.
    Convert relative dates like "tomorrow", "next Friday", "10th June", or "ten days from now"
    into absolute dueDateText values in yyyy-MM-dd format.

    Notification:
    $inputText
    """.trimIndent()

  private fun priorityPrompt(tasks: List<TaskItem>): String =
    """
    Current date/time: ${currentDateTimeContext()}.
    Prioritize these student deadlines. Return at most 4 nextActions.
    Prefer tasks due soon, high academic impact work, and tasks that unblock later work.
    Keep each action short and concrete.

    Deadlines:
    ${tasks.joinToString("\n") { task ->
      "id=${task.id}; title=${task.title}; due=${task.dueDateText}; source=${task.source.take(80)}"
    }}
    """.trimIndent()

  private fun localNextActions(tasks: List<TaskItem>): List<TaskPrioritySuggestion> {
    val today = LocalDate.now()
    return tasks
      .sortedWith(
        compareBy<TaskItem> { task -> DueDateResolver.parse(task.dueDateText, today) ?: LocalDate.MAX }
          .thenBy { it.id },
      )
      .take(4)
      .map { task ->
        val dueDate = DueDateResolver.parse(task.dueDateText, today)
        val daysUntil = dueDate?.let { ChronoUnit.DAYS.between(today, it) }
        TaskPrioritySuggestion(
          taskId = task.id,
          action = actionFor(task, daysUntil),
          reason = reasonFor(task, daysUntil),
          urgency = urgencyFor(daysUntil),
        )
      }
  }

  private fun actionFor(task: TaskItem, daysUntil: Long?): String =
    when {
      daysUntil != null && daysUntil <= 0 -> "Finish ${task.title}"
      daysUntil == 1L -> "Do the first pass on ${task.title}"
      daysUntil != null && daysUntil <= 3 -> "Block time for ${task.title}"
      else -> "Plan the next step for ${task.title}"
    }.take(90)

  private fun reasonFor(task: TaskItem, daysUntil: Long?): String =
    when {
      daysUntil == null -> "No parseable due date, so it stays visible until clarified."
      daysUntil < 0 -> "Due date has passed: ${task.dueDateText}."
      daysUntil == 0L -> "Due today: ${task.dueDateText}."
      daysUntil == 1L -> "Due tomorrow: ${task.dueDateText}."
      daysUntil <= 3 -> "Due soon: ${task.dueDateText}."
      else -> "Upcoming deadline: ${task.dueDateText}."
    }

  private fun urgencyFor(daysUntil: Long?): String =
    when {
      daysUntil == null -> "Next"
      daysUntil <= 1 -> "Now"
      daysUntil <= 3 -> "Today"
      daysUntil <= 7 -> "Next"
      else -> "Later"
    }

  private fun notificationFailureResult(summary: String): AgentResult =
    AgentResult(
      kind = AgentKind.Notification,
      summary = summary,
      modelStatusText = "LiteRT-LM",
    )

  private fun notificationFallbackResult(inputText: String, failureSummary: String): AgentResult =
    AgentFallbacks.structured(inputText)?.asNotificationResult()
      ?: notificationFailureResult(failureSummary)

  private fun AgentResult.asNotificationResult(): AgentResult =
    copy(kind = AgentKind.Notification)

  private fun currentDateTimeContext(): String =
    ZonedDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, HH:mm z", Locale.US))
}
