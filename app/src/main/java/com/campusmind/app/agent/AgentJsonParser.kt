package com.campusmind.app.agent

import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.AgentResult
import com.campusmind.app.model.ExpenseItem
import com.campusmind.app.model.Flashcard
import com.campusmind.app.model.RuntimeType
import com.campusmind.app.model.TaskItem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object AgentJsonParser {
  private val json = Json { ignoreUnknownKeys = true }

  fun parse(
    kind: AgentKind,
    rawText: String,
    sourceText: String,
    runtimeType: RuntimeType,
    runtimeStatusText: String,
  ): AgentResult? {
    val root = runCatching { json.parseToJsonElement(rawText.extractJsonObject()).jsonObject }.getOrNull() ?: return null
    val summary = root.string("summary") ?: return null

    return AgentResult(
      kind = kind,
      summary = summary,
      tasks = root.array("tasks").mapNotNull { item ->
        val obj = item as? JsonObject ?: return@mapNotNull null
        TaskItem(
          title = obj.string("title") ?: return@mapNotNull null,
          dueDateText = obj.string("dueDateText") ?: obj.string("due") ?: "Review soon",
          source = sourceText.take(140),
        )
      },
      flashcards = root.array("flashcards").mapNotNull { item ->
        val obj = item as? JsonObject ?: return@mapNotNull null
        Flashcard(
          front = obj.string("front") ?: return@mapNotNull null,
          back = obj.string("back") ?: return@mapNotNull null,
          source = sourceText.take(140),
        )
      },
      expenses = root.array("expenses").mapNotNull { item ->
        val obj = item as? JsonObject ?: return@mapNotNull null
        ExpenseItem(
          amountText = obj.string("amountText") ?: obj.string("amount") ?: return@mapNotNull null,
          category = obj.string("category") ?: "Student spend",
          merchant = obj.string("merchant") ?: "Receipt",
          source = sourceText.take(140),
        )
      },
      runtimeType = runtimeType,
      runtimeStatusText = runtimeStatusText,
    )
  }

  private fun JsonObject.string(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }

  private fun JsonObject.array(key: String): JsonArray =
    this[key] as? JsonArray ?: JsonArray(emptyList())

  private fun String.extractJsonObject(): String {
    val start = indexOf('{')
    val end = lastIndexOf('}')
    return if (start >= 0 && end >= start) substring(start, end + 1) else this
  }
}
