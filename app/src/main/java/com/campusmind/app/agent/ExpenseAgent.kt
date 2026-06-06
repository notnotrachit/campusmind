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
    return modelRunner.generateStructured(expensePrompt(inputText), AgentSchemas.expense)
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
    Log the spending in this note as one expense.
    $inputText
    """.trimIndent()
}
