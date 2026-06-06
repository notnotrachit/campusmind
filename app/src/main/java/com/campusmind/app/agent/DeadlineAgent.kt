package com.campusmind.app.agent

import com.campusmind.app.ai.SingleModelRunner
import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.AgentResult
import com.campusmind.app.model.CAMPUS_MODEL_STATUS
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class DeadlineAgent(
  private val modelRunner: SingleModelRunner,
) : StudentAgent {
  override val kind = AgentKind.Deadline

  override suspend fun analyze(inputText: String): AgentResult {
    return modelRunner.generateStructured(deadlinePrompt(inputText), AgentSchemas.deadline)
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
    Extract the deadline from this note. Always produce one task with a clear, specific title.
    Put any due date in dueDateText as an absolute yyyy-MM-dd date.
    Current date/time for resolving relative due dates: ${currentDateTimeContext()}.
    Convert relative dates like "tomorrow", "next Friday", "10th June", or "ten days from now"
    into absolute dueDateText values in yyyy-MM-dd format.
    $inputText
    """.trimIndent()

  private fun currentDateTimeContext(): String =
    ZonedDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy, HH:mm z", Locale.US))
}
