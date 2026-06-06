package com.campusmind.app.agent

import com.campusmind.app.ai.RuntimeOrchestrator
import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.AgentResult
import com.campusmind.app.model.ExpenseItem

class ExpenseAgent(
  private val runtimeOrchestrator: RuntimeOrchestrator? = null,
) : StudentAgent {
  override val kind = AgentKind.Expense

  override suspend fun analyze(inputText: String): AgentResult {
    var fallbackStatus = "Deterministic fallback"
    runtimeOrchestrator?.generate(expensePrompt(inputText))?.getOrNull()?.let { generation ->
      AgentJsonParser.parse(kind, generation.text, inputText, generation.runtimeType, generation.statusText)?.let { return it }
      fallbackStatus = "Deterministic fallback after malformed LLM response from ${generation.runtimeType.name}"
    }

    val amount = Regex("""(?:rs\.?|inr|₹)\s?(\d+(?:\.\d{1,2})?)""", RegexOption.IGNORE_CASE)
      .find(inputText)
      ?.value
      ?: Regex("""\b\d{2,5}(?:\.\d{1,2})?\b""").find(inputText)?.value
      ?: "Amount unknown"

    val category = when {
      inputText.contains("canteen", ignoreCase = true) || inputText.contains("food", ignoreCase = true) -> "Food"
      inputText.contains("uber", ignoreCase = true) || inputText.contains("metro", ignoreCase = true) -> "Travel"
      inputText.contains("book", ignoreCase = true) || inputText.contains("print", ignoreCase = true) -> "Academics"
      else -> "Student spend"
    }

    val merchant = inputText.lineSequence().firstOrNull { it.isNotBlank() }?.take(48) ?: "Receipt"
    return AgentResult(
      kind = kind,
      summary = "Logged one expense from the inbox item.",
      runtimeStatusText = fallbackStatus,
      expenses = listOf(
        ExpenseItem(
          amountText = amount,
          category = category,
          merchant = merchant,
          source = inputText.take(140),
        ),
      ),
    )
  }

  private fun expensePrompt(inputText: String): String =
    """
    You are CampusMind's Expense agent. Convert the receipt or payment text into JSON only.
    Schema: {"summary":"short summary","expenses":[{"amountText":"amount with currency","category":"Food|Travel|Academics|Student spend","merchant":"merchant"}]}
    Create one expense. Do not include markdown.
    Input:
    $inputText
    """.trimIndent()
}
