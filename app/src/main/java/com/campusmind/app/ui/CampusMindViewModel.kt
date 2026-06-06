package com.campusmind.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
  val modelConfig: ModelConfig = ModelConfig(),
)

class CampusMindViewModel(
  private val repository: CampusMindRepository,
  private val modelSettingsStore: ModelSettingsStore,
) : ViewModel() {
  private val transient = kotlinx.coroutines.flow.MutableStateFlow(CampusMindUiState())

  val uiState: StateFlow<CampusMindUiState> =
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
      modelSettingsStore.config,
    ) { current, modelConfig ->
      current.copy(modelConfig = modelConfig)
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
      transient.value = transient.value.copy(isProcessing = true, status = "Routing local agent...")
      val result = repository.submitText(content)
      transient.value = transient.value.copy(
        inputText = "",
        isProcessing = false,
        status = result.summary,
      )
    }
  }

  fun saveModelPath(path: String) {
    viewModelScope.launch {
      modelSettingsStore.save(uiState.value.modelConfig.copy(modelPath = path.trim()))
      transient.value = transient.value.copy(status = "Model path saved")
    }
  }
}

class CampusMindViewModelFactory(
  private val repository: CampusMindRepository,
  private val modelSettingsStore: ModelSettingsStore,
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T =
    CampusMindViewModel(repository, modelSettingsStore) as T
}
