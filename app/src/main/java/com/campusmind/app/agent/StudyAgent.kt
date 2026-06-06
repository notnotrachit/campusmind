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
    return modelRunner.generateStructured(studyPrompt(inputText), AgentSchemas.study)
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
    Turn this note into 2 concise revision flashcards.
    $inputText
    """.trimIndent()
}
