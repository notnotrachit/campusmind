package com.campusmind.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.campusmind.app.ai.RuntimeState
import com.campusmind.app.model.ActivityLog
import com.campusmind.app.model.ExpenseItem
import com.campusmind.app.model.Flashcard
import com.campusmind.app.model.TaskItem

private enum class Tab(val label: String) {
  Inbox("Inbox"),
  Results("Results"),
  Model("AI Runtime"),
  Activity("Activity"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusMindApp(viewModel: CampusMindViewModel) {
  val state by viewModel.uiState.collectAsState()
  var selectedTab by remember { mutableStateOf(Tab.Inbox) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text("CampusMind", fontWeight = FontWeight.SemiBold)
            Text(
              text = state.status,
              style = MaterialTheme.typography.labelMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        },
      )
    },
    bottomBar = {
      NavigationBar {
        Tab.entries.forEach { tab ->
          NavigationBarItem(
            selected = selectedTab == tab,
            onClick = { selectedTab = tab },
            icon = { Text(tab.label.first().toString()) },
            label = { Text(tab.label) },
          )
        }
      }
    },
  ) { padding ->
    when (selectedTab) {
      Tab.Inbox -> InboxScreen(
        modifier = Modifier.padding(padding),
        inputText = state.inputText,
        isProcessing = state.isProcessing,
        onInputChange = viewModel::updateInput,
        onSubmit = viewModel::submitInput,
      )
      Tab.Results -> ResultsScreen(
        modifier = Modifier.padding(padding),
        tasks = state.tasks,
        flashcards = state.flashcards,
        expenses = state.expenses,
      )
      Tab.Model -> ModelScreen(
        modifier = Modifier.padding(padding),
        modelPath = state.modelConfig.modelPath,
        backend = state.modelConfig.backend,
        maxTokens = state.modelConfig.maxTokens,
        aiCoreMode = "${state.modelConfig.aiCoreReleaseStage.name} · ${state.modelConfig.aiCorePreference.name}",
        runtimeState = state.runtimeState,
        onSavePath = viewModel::saveModelPath,
      )
      Tab.Activity -> ActivityScreen(
        modifier = Modifier.padding(padding),
        logs = state.logs,
      )
    }
  }
}

@Composable
private fun InboxScreen(
  modifier: Modifier,
  inputText: String,
  isProcessing: Boolean,
  onInputChange: (String) -> Unit,
  onSubmit: () -> Unit,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Text("Chaos Inbox", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
      Text(
        "Paste a WhatsApp notice, lecture note, receipt text, or reminder. The local router sends it to a student agent.",
        style = MaterialTheme.typography.bodyMedium,
      )
    }
    item {
      OutlinedTextField(
        value = inputText,
        onValueChange = onInputChange,
        modifier = Modifier.fillMaxWidth().height(180.dp),
        label = { Text("Student input") },
        placeholder = { Text("Submit DBMS assignment by Friday...") },
      )
    }
    item {
      Button(
        onClick = onSubmit,
        enabled = !isProcessing,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(if (isProcessing) "Processing" else "Run local agent")
      }
    }
    item {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = true, onClick = {}, label = { Text("Text") })
        FilterChip(selected = false, onClick = {}, label = { Text("Camera soon") })
        FilterChip(selected = false, onClick = {}, label = { Text("Voice soon") })
      }
    }
  }
}

@Composable
private fun ResultsScreen(
  modifier: Modifier,
  tasks: List<TaskItem>,
  flashcards: List<Flashcard>,
  expenses: List<ExpenseItem>,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item { SectionTitle("Deadlines") }
    if (tasks.isEmpty()) item { EmptyCard("No deadlines yet") }
    items(tasks) { task -> ResultCard(task.title, task.dueDateText) }

    item { SectionTitle("Flashcards") }
    if (flashcards.isEmpty()) item { EmptyCard("No flashcards yet") }
    items(flashcards) { card -> ResultCard(card.front, card.back) }

    item { SectionTitle("Expenses") }
    if (expenses.isEmpty()) item { EmptyCard("No expenses yet") }
    items(expenses) { expense -> ResultCard("${expense.merchant} · ${expense.amountText}", expense.category) }
  }
}

@Composable
private fun ModelScreen(
  modifier: Modifier,
  modelPath: String,
  backend: String,
  maxTokens: Int,
  aiCoreMode: String,
  runtimeState: RuntimeState,
  onSavePath: (String) -> Unit,
) {
  var draftPath by remember(modelPath) { mutableStateOf(modelPath) }
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Text("AI Runtime", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
      Text("AI Core is attempted first. LiteRT LM uses this manual model path when AI Core is unavailable.")
    }
    item { ResultCard("AI Core", "${runtimeState.statusText} · $aiCoreMode") }
    if (runtimeState.lastFailureReason.isNotBlank()) {
      item { ResultCard("Fallback reason", runtimeState.lastFailureReason) }
    }
    item {
      OutlinedTextField(
        value = draftPath,
        onValueChange = { draftPath = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Local model path") },
        placeholder = { Text("/sdcard/Download/model.task") },
      )
    }
    item {
      Button(onClick = { onSavePath(draftPath) }, modifier = Modifier.fillMaxWidth()) {
        Text("Save LiteRT fallback path")
      }
    }
    item { ResultCard("Active runtime", runtimeState.activeRuntime.name) }
    item { ResultCard("Backend", "$backend · max $maxTokens tokens") }
  }
}

@Composable
private fun ActivityScreen(modifier: Modifier, logs: List<ActivityLog>) {
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item { SectionTitle("Local Agent Activity") }
    if (logs.isEmpty()) item { EmptyCard("No local actions yet") }
    items(logs) { log -> ResultCard(log.message, "Stored on device") }
  }
}

@Composable
private fun SectionTitle(text: String) {
  Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun EmptyCard(text: String) {
  ResultCard(title = text, body = "Run an inbox item to populate this view.")
}

@Composable
private fun ResultCard(title: String, body: String) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(14.dp)) {
      Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
      Spacer(Modifier.height(4.dp))
      Text(body, style = MaterialTheme.typography.bodyMedium)
    }
  }
}
