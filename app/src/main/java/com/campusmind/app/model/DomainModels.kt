package com.campusmind.app.model

enum class InboxType {
  Text,
  Image,
  File,
  Voice,
  Notification,
}

enum class AgentKind {
  Study,
  Deadline,
  Expense,
  Notification,
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
  val modelStatusText: String = "LiteRT-LM",
)

data class ModelConfig(
  val modelPath: String = "",
  val modelName: String = CAMPUS_MODEL_NAME,
  val modelId: String = CAMPUS_MODEL_ID,
  val modelFile: String = CAMPUS_MODEL_FILE,
  val commitHash: String = CAMPUS_MODEL_COMMIT,
  val sizeInBytes: Long = CAMPUS_MODEL_SIZE_BYTES,
  val backend: String = "CPU",
  val maxTokens: Int = CAMPUS_MODEL_MAX_TOKENS,
  val temperature: Float = 0.4f,
  val topK: Int = 40,
  val topP: Float = 0.95f,
)
