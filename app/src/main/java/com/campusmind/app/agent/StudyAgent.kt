package com.campusmind.app.agent

import com.campusmind.app.ai.RuntimeOrchestrator
import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.AgentResult
import com.campusmind.app.model.Flashcard

class StudyAgent(
  private val runtimeOrchestrator: RuntimeOrchestrator? = null,
) : StudentAgent {
  override val kind = AgentKind.Study

  override suspend fun analyze(inputText: String): AgentResult {
    var fallbackStatus = "Deterministic fallback"
    runtimeOrchestrator?.generate(studyPrompt(inputText))?.getOrNull()?.let { generation ->
      AgentJsonParser.parse(kind, generation.text, inputText, generation.runtimeType, generation.statusText)?.let { return it }
      fallbackStatus = "Deterministic fallback after malformed LLM response from ${generation.runtimeType.name}"
    }

    val words = inputText.split(Regex("\\s+")).filter { it.length > 4 }
    val concept = words.firstOrNull() ?: "Topic"
    val detail = inputText.lineSequence().firstOrNull { it.isNotBlank() }?.take(100) ?: inputText.take(100)

    return AgentResult(
      kind = kind,
      summary = "Generated two starter flashcards from the study material.",
      runtimeStatusText = fallbackStatus,
      flashcards = listOf(
        Flashcard(
          front = "What is the key idea in this note?",
          back = detail.ifBlank { "Review the source note." },
          source = inputText.take(140),
        ),
        Flashcard(
          front = "Explain $concept in one line.",
          back = "Use the source note to connect $concept with the surrounding topic.",
          source = inputText.take(140),
        ),
      ),
    )
  }

  private fun studyPrompt(inputText: String): String =
    """
    You are CampusMind's Study agent. Convert the note into JSON only.
    Schema: {"summary":"short summary","flashcards":[{"front":"question","back":"answer"}]}
    Create 2 concise flashcards. Do not include markdown.
    Input:
    $inputText
    """.trimIndent()
}
