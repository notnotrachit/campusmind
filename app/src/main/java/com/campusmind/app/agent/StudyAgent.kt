package com.campusmind.app.agent

import com.campusmind.app.ai.SingleModelRunner
import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.AgentResult
import com.campusmind.app.model.CAMPUS_MODEL_STATUS

class StudyAgent(
  private val modelRunner: SingleModelRunner,
) : StudentAgent {
  override val kind = AgentKind.Study

  override suspend fun analyze(inputText: String): AgentResult {
    return modelRunner.generate(studyPrompt(inputText))
      .fold(
        onSuccess = { text ->
          AgentJsonParser.parse(kind, text, inputText, CAMPUS_MODEL_STATUS)
            ?: error("Model returned malformed Study JSON")
        },
        onFailure = { error -> throw IllegalStateException("Study model failed after retries", error) },
      )
  }

  private fun studyPrompt(inputText: String): String =
    """
    JSON only. Schema {"summary":"short","flashcards":[{"front":"question","back":"answer"}]}.
    Make 2 concise flashcards.
    $inputText
    """.trimIndent()
}
