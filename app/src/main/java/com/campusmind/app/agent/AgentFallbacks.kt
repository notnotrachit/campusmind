package com.campusmind.app.agent

import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.AgentResult
import com.campusmind.app.model.ExpenseItem
import com.campusmind.app.model.Flashcard
import com.campusmind.app.model.TaskItem

private const val FALLBACK_STATUS = "Local fallback"

object AgentFallbacks {
  fun structured(inputText: String): AgentResult? {
    val clean = inputText.cleanSource()
    if (clean.isBlank()) return null

    val parts = clean.parts()
    val tasks = parts.mapNotNull { part -> part.toTask(source = clean) }
    val expenses = parts.mapNotNull { part -> part.toExpense(source = clean) }
    val flashcards = if (tasks.isEmpty() && expenses.isEmpty() && clean.looksLikeStudyNote()) {
      study(clean).flashcards
    } else {
      emptyList()
    }

    if (tasks.isEmpty() && expenses.isEmpty() && flashcards.isEmpty()) return null

    val kind = when {
      tasks.isNotEmpty() -> AgentKind.Deadline
      expenses.isNotEmpty() -> AgentKind.Expense
      else -> AgentKind.Study
    }
    return AgentResult(
      kind = kind,
      summary = summaryFor(tasks.size, flashcards.size, expenses.size),
      tasks = tasks,
      flashcards = flashcards,
      expenses = expenses,
      modelStatusText = FALLBACK_STATUS,
    )
  }

  fun study(inputText: String, reason: Throwable? = null): AgentResult {
    val clean = inputText.cleanSource()
    val sentence = clean.firstSentence()
    val topic = sentence.take(70).ifBlank { "Campus note" }
    return AgentResult(
      kind = AgentKind.Study,
      summary = "Created revision cards from the note",
      flashcards = listOf(
        Flashcard(
          front = "What is the key point?",
          back = sentence.ifBlank { clean.take(120).ifBlank { "Review the pasted note." } },
          source = clean.take(140),
        ),
        Flashcard(
          front = "What should I revise next?",
          back = topic,
          source = clean.take(140),
        ),
      ),
      modelStatusText = FALLBACK_STATUS,
    )
  }

  fun deadline(inputText: String, reason: Throwable? = null): AgentResult {
    val clean = inputText.cleanSource()
    val task = clean.toTask(source = clean)
    return AgentResult(
      kind = AgentKind.Deadline,
      summary = if (task == null) "No deadline found" else "Created a deadline",
      tasks = listOfNotNull(task),
      modelStatusText = FALLBACK_STATUS,
    )
  }

  fun expense(inputText: String, reason: Throwable? = null): AgentResult {
    val clean = inputText.cleanSource()
    val expense = clean.toExpense(source = clean)
    return AgentResult(
      kind = AgentKind.Expense,
      summary = if (expense == null) "No expense found" else "Logged an expense",
      expenses = listOfNotNull(expense),
      modelStatusText = FALLBACK_STATUS,
    )
  }

  private val amountRegex = Regex("""(?:₹|rs\.?|inr)\s*\d+(?:[.,]\d+)?|\d+(?:[.,]\d+)?\s*(?:₹|rs\.?|inr)""", RegexOption.IGNORE_CASE)
  private val dueRegex = Regex(
    """\b(?:today|tomorrow|tonight|monday|tuesday|wednesday|thursday|friday|saturday|sunday|by\s+(?:\d{1,2}(?:st|nd|rd|th)?\s+)?(?:jan|january|feb|february|mar|march|apr|april|may|jun|june|jul|july|aug|august|sep|sept|september|oct|october|nov|november|dec|december|\w+)|due\s+(?:on\s+)?(?:\d{1,2}(?:st|nd|rd|th)?\s+)?(?:jan|january|feb|february|mar|march|apr|april|may|jun|june|jul|july|aug|august|sep|sept|september|oct|october|nov|november|dec|december|\w+)|deadline\s+(?:is\s+|on\s+)?\d{1,2}(?:st|nd|rd|th)?\s+(?:jan|january|feb|february|mar|march|apr|april|may|jun|june|jul|july|aug|august|sep|sept|september|oct|october|nov|november|dec|december)|\d{1,2}(?:st|nd|rd|th)?\s+(?:jan|january|feb|february|mar|march|apr|april|may|jun|june|jul|july|aug|august|sep|sept|september|oct|october|nov|november|dec|december)|\d{1,2}[/-]\d{1,2}(?:[/-]\d{2,4})?)\b""",
    RegexOption.IGNORE_CASE,
  )
  private val merchantRegex = Regex("""(?:at|from|to|merchant[:\s]+)\s+([A-Za-z][A-Za-z0-9 &.-]{2,40})""", RegexOption.IGNORE_CASE)
  private val deadlineIntentRegex = Regex("""\b(?:deadline|due|submit|submission|assignment|exam|quiz|project|remind)\b""", RegexOption.IGNORE_CASE)
  private val deadlineTitleNoiseRegex = Regex("""\b(?:deadline|due|submit|submission|remind|reminder)\b""", RegexOption.IGNORE_CASE)
  private val studyIntentRegex = Regex("""\b(?:note|notes|revise|revision|study|explain|chapter|topic|lecture|definition|formula|flashcard)\b""", RegexOption.IGNORE_CASE)

  private fun String.toTask(source: String): TaskItem? {
    val due = dueRegex.find(this)?.value?.cleanDuePhrase() ?: return null
    if (!hasDeadlineSignal()) return null
    val title = titleBeforeDue(due)
      .ifBlank { titleAfterDue() }
      .ifBlank { "Campus deadline" }
      .take(90)
    return TaskItem(
      title = title,
      dueDateText = due,
      source = source.take(140),
    )
  }

  private fun String.toExpense(source: String): ExpenseItem? {
    val amount = amountRegex.find(this)?.value ?: return null
    val merchant = merchantRegex.find(this)?.groupValues?.getOrNull(1)?.trim()
      ?: firstSentence().take(40).ifBlank { "Receipt" }
    return ExpenseItem(
      amountText = amount,
      category = inferCategory(this),
      merchant = merchant,
      source = source.take(140),
    )
  }

  private fun String.titleBeforeDue(due: String): String =
    substringBefore(due, this)
      .replace(deadlineTitleNoiseRegex, "")
      .replace(Regex("""\b(?:is|on|by|for|the)\b""", RegexOption.IGNORE_CASE), " ")
      .replace(Regex("""\s+"""), " ")
      .trim(' ', '-', ':', ',', '.')

  private fun String.titleAfterDue(): String =
    replace(dueRegex, "")
      .replace(deadlineTitleNoiseRegex, "")
      .replace(Regex("""\b(?:is|on|by|for|the)\b""", RegexOption.IGNORE_CASE), " ")
      .replace(Regex("""\s+"""), " ")
      .trim(' ', '-', ':', ',', '.')

  private fun String.cleanDuePhrase(): String =
    replace(Regex("""^(?:deadline|due)\s+(?:is\s+|on\s+)?""", RegexOption.IGNORE_CASE), "")
      .trim()

  private fun String.looksLikeStudyNote(): Boolean =
    studyIntentRegex.containsMatchIn(this) || length > 80

  private fun String.hasDeadlineSignal(): Boolean =
    deadlineIntentRegex.containsMatchIn(this) ||
      Regex("""\b(?:today|tomorrow|tonight|by\s+|due\s+|deadline\s+)""", RegexOption.IGNORE_CASE).containsMatchIn(this)

  private fun inferCategory(text: String): String {
    val lower = text.lowercase()
    return when {
      listOf("canteen", "food", "coffee", "mess", "restaurant", "snack").any(lower::contains) -> "Food"
      listOf("cab", "auto", "bus", "metro", "train", "travel").any(lower::contains) -> "Travel"
      listOf("book", "print", "lab", "course", "tuition", "academ").any(lower::contains) -> "Academics"
      else -> "Student spend"
    }
  }

  private fun String.cleanSource(): String =
    lineSequence()
      .map { it.trim() }
      .filter { it.isNotEmpty() }
      .joinToString(" ")

  private fun String.parts(): List<String> =
    split(Regex("""(?:\n|[;•]|(?:\s+-\s+)|(?:\s+and\s+(?=\w)))""", RegexOption.IGNORE_CASE))
      .map { it.trim() }
      .filter { it.isNotEmpty() }

  private fun String.firstSentence(): String =
    split('.', '\n').firstOrNull { it.isNotBlank() }?.trim().orEmpty()

  private fun summaryFor(taskCount: Int, flashcardCount: Int, expenseCount: Int): String =
    listOfNotNull(
      taskCount.takeIf { it > 0 }?.let { "$it deadline${if (it == 1) "" else "s"}" },
      flashcardCount.takeIf { it > 0 }?.let { "$it flashcard${if (it == 1) "" else "s"}" },
      expenseCount.takeIf { it > 0 }?.let { "$it expense${if (it == 1) "" else "s"}" },
    ).joinToString(", ", prefix = "Created ")
}
