package com.campusmind.app.agent

import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.AgentResult
import com.campusmind.app.model.ExpenseItem

class ExpenseAgent : StudentAgent {
  override val kind = AgentKind.Expense

  override suspend fun analyze(inputText: String): AgentResult {
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
}
