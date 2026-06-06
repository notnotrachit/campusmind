package com.campusmind.app.agent

import com.campusmind.app.ai.SingleModelRunner
import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.AgentResult
import com.campusmind.app.model.CAMPUS_MODEL_STATUS

class ExpenseAgent(
  private val modelRunner: SingleModelRunner,
) : StudentAgent {
  override val kind = AgentKind.Expense

  override suspend fun analyze(inputText: String): AgentResult {
    return modelRunner.generate(expensePrompt(inputText))
      .fold(
        onSuccess = { text ->
          AgentJsonParser.parse(kind, text, inputText, CAMPUS_MODEL_STATUS)
            ?: error("Model returned malformed Expense JSON")
        },
        onFailure = { error -> throw IllegalStateException("Expense model failed after retries", error) },
      )
  }

  private fun expensePrompt(inputText: String): String =
    """
    JSON only. Schema {"summary":"short","expenses":[{"amountText":"amount","category":"Food|Travel|Academics|Student spend","merchant":"merchant"}]}.
    Create one expense.
    $inputText
    """.trimIndent()
}
