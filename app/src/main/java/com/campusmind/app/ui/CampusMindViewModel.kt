package com.campusmind.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.campusmind.app.ai.ModelDownloadManager
import com.campusmind.app.ai.ModelDownloadState
import com.campusmind.app.data.CampusMindRepository
import com.campusmind.app.model.ActivityLog
import com.campusmind.app.model.ExpenseItem
import com.campusmind.app.model.Flashcard
import com.campusmind.app.model.ModelConfig
import com.campusmind.app.model.TaskItem
import com.campusmind.app.settings.ModelSettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CampusMindUiState(
  val inputText: String = "",
  val isProcessing: Boolean = false,
  val status: String = "Ready",
  val tasks: List<TaskItem> = emptyList(),
  val flashcards: List<Flashcard> = emptyList(),
  val expenses: List<ExpenseItem> = emptyList(),
  val logs: List<ActivityLog> = emptyList(),
  val inboxCount: Int = 0,
  val modelConfig: ModelConfig = ModelConfig(),
  val modelDownloadState: ModelDownloadState = ModelDownloadState(),
)

class CampusMindViewModel(
  private val repository: CampusMindRepository,
  private val modelSettingsStore: ModelSettingsStore,
  private val modelDownloadManager: ModelDownloadManager,
) : ViewModel() {
  private val transient = kotlinx.coroutines.flow.MutableStateFlow(CampusMindUiState())

  val uiState: StateFlow<CampusMindUiState> =
    combine(
      combine(
        combine(
          transient,
          repository.tasks,
          repository.flashcards,
          repository.expenses,
          repository.logs,
        ) { current, tasks, flashcards, expenses, logs ->
          current.copy(
            tasks = tasks,
            flashcards = flashcards,
            expenses = expenses,
            logs = logs,
          )
        },
        repository.inbox,
      ) { current, inbox -> current.copy(inboxCount = inbox.size) },
      modelSettingsStore.config,
      modelDownloadManager.state,
    ) { current, modelConfig, modelDownloadState ->
      current.copy(
        modelConfig = modelConfig,
        modelDownloadState = modelDownloadState,
      )
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = CampusMindUiState(),
    )

  fun updateInput(text: String) {
    transient.value = transient.value.copy(inputText = text)
  }

  fun submitInput() {
    val content = transient.value.inputText.trim()
    if (content.isBlank()) {
      transient.value = transient.value.copy(status = "Add text before running an agent")
      return
    }

    viewModelScope.launch {
      transient.value = transient.value.copy(isProcessing = true, status = "Classifying mock notification...")
      runCatching { repository.submitMockNotification(content) }
        .onSuccess { result ->
          transient.value = transient.value.copy(
            inputText = "",
            isProcessing = false,
            status = result.summary,
          )
        }
        .onFailure { error ->
          transient.value = transient.value.copy(
            isProcessing = false,
            status = "Model run failed: ${error.message ?: error::class.java.simpleName}",
          )
        }
    }
  }

  fun saveModelPath(path: String) {
    viewModelScope.launch {
      modelSettingsStore.save(uiState.value.modelConfig.copy(modelPath = path.trim()))
      transient.value = transient.value.copy(status = "Model path saved")
    }
  }

  fun downloadModel(url: String) {
    val trimmedUrl = url.trim()
    if (trimmedUrl.isBlank()) {
      transient.value = transient.value.copy(status = "Add a model URL before downloading")
      return
    }

    viewModelScope.launch {
      transient.value = transient.value.copy(status = "Downloading ${uiState.value.modelConfig.modelName}...")
      val result = modelDownloadManager.download(trimmedUrl, uiState.value.modelConfig.modelFile)
      result.fold(
        onSuccess = { path ->
          modelSettingsStore.save(uiState.value.modelConfig.copy(modelPath = path))
          transient.value = transient.value.copy(status = "${uiState.value.modelConfig.modelName} downloaded and saved")
        },
        onFailure = { error ->
          transient.value = transient.value.copy(
            status = "Model download failed: ${error.message ?: error::class.java.simpleName}",
          )
        },
      )
    }
  }
}

class CampusMindViewModelFactory(
  private val repository: CampusMindRepository,
  private val modelSettingsStore: ModelSettingsStore,
  private val modelDownloadManager: ModelDownloadManager,
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T =
    CampusMindViewModel(repository, modelSettingsStore, modelDownloadManager) as T
}
