package com.campusmind.app.model

enum class InboxType {
  Text,
  Image,
  File,
  Voice,
}

enum class AgentKind {
  Study,
  Deadline,
  Expense,
}

enum class RuntimeType {
  AICORE,
  LITERT_LM,
  STUB,
}

enum class AiCoreReleaseStage {
  STABLE,
  PREVIEW,
}

enum class AiCorePreference {
  FAST,
  FULL,
}

data class InboxItem(
  val id: Long = 0,
  val type: InboxType,
  val content: String,
  val createdAtMillis: Long = System.currentTimeMillis(),
)

data class TaskItem(
  val id: Long = 0,
  val title: String,
  val dueDateText: String,
  val source: String,
  val done: Boolean = false,
)

data class Flashcard(
  val id: Long = 0,
  val front: String,
  val back: String,
  val source: String,
)

data class ExpenseItem(
  val id: Long = 0,
  val amountText: String,
  val category: String,
  val merchant: String,
  val source: String,
)

data class ActivityLog(
  val id: Long = 0,
  val message: String,
  val createdAtMillis: Long = System.currentTimeMillis(),
)

data class AgentResult(
  val kind: AgentKind,
  val summary: String,
  val tasks: List<TaskItem> = emptyList(),
  val flashcards: List<Flashcard> = emptyList(),
  val expenses: List<ExpenseItem> = emptyList(),
  val runtimeType: RuntimeType = RuntimeType.STUB,
  val runtimeStatusText: String = "Deterministic fallback",
)

data class ModelConfig(
  val modelPath: String = "",
  val backend: String = "GPU",
  val maxTokens: Int = 512,
  val temperature: Float = 0.4f,
  val runtimeType: RuntimeType = RuntimeType.AICORE,
  val aiCoreReleaseStage: AiCoreReleaseStage = AiCoreReleaseStage.STABLE,
  val aiCorePreference: AiCorePreference = AiCorePreference.FAST,
  val topK: Int = 40,
  val maxOutputTokens: Int = 512,
  val runtimeStatusText: String = "AI Core is the default runtime",
)
