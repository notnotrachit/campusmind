package com.campusmind.app.agent

import com.campusmind.app.ai.SingleModelRunner
import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.AgentResult
import com.campusmind.app.model.CAMPUS_MODEL_STATUS

class DeadlineAgent(
  private val modelRunner: SingleModelRunner,
) : StudentAgent {
  override val kind = AgentKind.Deadline

  override suspend fun analyze(inputText: String): AgentResult {
    return modelRunner.generate(deadlinePrompt(inputText))
      .fold(
        onSuccess = { text ->
          AgentJsonParser.parse(kind, text, inputText, CAMPUS_MODEL_STATUS)
            ?: error("Model returned malformed Deadline JSON")
        },
        onFailure = { error -> throw IllegalStateException("Deadline model failed after retries", error) },
      )
  }

  private fun deadlinePrompt(inputText: String): String =
    """
    JSON only. Schema {"summary":"short","tasks":[{"title":"task","dueDateText":"due"}]}.
    Create a task only if there is a real due date. Otherwise return {"summary":"No deadline found","tasks":[]}.
    $inputText
    """.trimIndent()
}
