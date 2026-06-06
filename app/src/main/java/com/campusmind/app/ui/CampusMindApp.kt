package com.campusmind.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.campusmind.app.ai.ModelDownloadState
import com.campusmind.app.model.ExpenseItem
import com.campusmind.app.model.Flashcard
import com.campusmind.app.model.TaskItem
import com.campusmind.app.model.TaskPrioritySuggestion
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private enum class Tab(val label: String, val icon: ImageVector) {
  Inbox("Inbox", Icons.Rounded.Inbox),
  Deadlines("Deadlines", Icons.Rounded.EventBusy),
  Study("Study", Icons.Rounded.MenuBook),
  Model("Runtime", Icons.Rounded.Memory),
}

private val bottomTabs = listOf(Tab.Inbox, Tab.Deadlines, Tab.Study)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusMindApp(viewModel: CampusMindViewModel) {
  val state by viewModel.uiState.collectAsState()
  var selectedTab by remember { mutableStateOf(Tab.Inbox) }
  val ready = state.modelConfig.modelPath.isNotBlank()

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background,
          titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        title = {
          Column {
            Text(
              "CampusMind",
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = state.status,
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.secondary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        },
        actions = {
          StatusPill(
            text = if (ready) "Offline ready" else "Model needed",
            ready = ready,
          )
          IconButton(onClick = { selectedTab = Tab.Model }) {
            Icon(
              Icons.Rounded.Settings,
              contentDescription = "AI Runtime settings",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
      )
    },
    bottomBar = {
      NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        bottomTabs.forEach { tab ->
          NavigationBarItem(
            selected = selectedTab == tab,
            onClick = { selectedTab = tab },
            icon = { Icon(tab.icon, contentDescription = tab.label) },
            label = { Text(tab.label) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
              selectedTextColor = MaterialTheme.colorScheme.primary,
              indicatorColor = MaterialTheme.colorScheme.primaryContainer,
              unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
              unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
          )
        }
      }
    },
  ) { padding ->
    val contentModifier = Modifier.padding(padding)
    when (selectedTab) {
      Tab.Inbox -> InboxScreen(
        modifier = contentModifier,
        inputText = state.inputText,
        isProcessing = state.isProcessing,
        notificationCount = state.inboxCount,
        deadlineCount = state.tasks.size,
        flashcardCount = state.flashcards.size,
        tasks = state.tasks,
        nextActions = state.nextActions,
        isPrioritizing = state.isPrioritizing,
        priorityStatus = state.priorityStatus,
        onInputChange = viewModel::updateInput,
        onSubmit = viewModel::submitInput,
        onJumpToDeadlines = { selectedTab = Tab.Deadlines },
        onCompleteAction = viewModel::deleteTask,
      )
      Tab.Deadlines -> DeadlinesScreen(
        modifier = contentModifier,
        tasks = state.tasks,
        onDeleteTask = viewModel::deleteTask,
        onJumpToInbox = { selectedTab = Tab.Inbox },
      )
      Tab.Study -> StudyScreen(
        modifier = contentModifier,
        flashcards = state.flashcards,
        expenses = state.expenses,
        onJumpToInbox = { selectedTab = Tab.Inbox },
      )
      Tab.Model -> ModelScreen(
        modifier = contentModifier,
        modelName = state.modelConfig.modelName,
        modelId = state.modelConfig.modelId,
        modelFile = state.modelConfig.modelFile,
        commitHash = state.modelConfig.commitHash,
        sizeInBytes = state.modelConfig.sizeInBytes,
        modelPath = state.modelConfig.modelPath,
        backend = state.modelConfig.backend,
        maxTokens = state.modelConfig.maxTokens,
        modelDownloadState = state.modelDownloadState,
        onSavePath = viewModel::saveModelPath,
        onDownloadModel = viewModel::downloadModel,
      )
    }
  }
}

// ---------------------------------------------------------------------------
// Inbox
// ---------------------------------------------------------------------------

@Composable
private fun InboxScreen(
  modifier: Modifier,
  inputText: String,
  isProcessing: Boolean,
  notificationCount: Int,
  deadlineCount: Int,
  flashcardCount: Int,
  tasks: List<TaskItem>,
  nextActions: List<TaskPrioritySuggestion>,
  isPrioritizing: Boolean,
  priorityStatus: String,
  onInputChange: (String) -> Unit,
  onSubmit: () -> Unit,
  onJumpToDeadlines: () -> Unit,
  onCompleteAction: (Long) -> Unit,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      HeroPanel(
        icon = Icons.Rounded.NotificationsActive,
        title = "Notification deadline agent",
        body = "Sorting your incoming chaos.",
      )
    }
    item {
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        StatTile("Seen", notificationCount.toString(), Icons.Rounded.Visibility, Modifier.weight(1f))
        StatTile("Deadlines", deadlineCount.toString(), Icons.Rounded.EventBusy, Modifier.weight(1f))
        StatTile("Cards", flashcardCount.toString(), Icons.Rounded.Style, Modifier.weight(1f))
      }
    }
    item {
      NextActionsPanel(
        nextActions = nextActions,
        tasks = tasks,
        isPrioritizing = isPrioritizing,
        priorityStatus = priorityStatus,
        onJumpToDeadlines = onJumpToDeadlines,
        onCompleteAction = onCompleteAction,
      )
    }
    item {
      InputPanel(
        inputText = inputText,
        isProcessing = isProcessing,
        onInputChange = onInputChange,
        onSubmit = onSubmit,
      )
    }
  }
}

@Composable
private fun NextActionsPanel(
  nextActions: List<TaskPrioritySuggestion>,
  tasks: List<TaskItem>,
  isPrioritizing: Boolean,
  priorityStatus: String,
  onJumpToDeadlines: () -> Unit,
  onCompleteAction: (Long) -> Unit,
) {
  CampusCard {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
          IconBadge(icon = Icons.Rounded.Bolt, tint = MaterialTheme.colorScheme.primary)
          Column {
            Text("Do Next", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
              priorityStatus,
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
        TextButton(onClick = onJumpToDeadlines) {
          Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.size(6.dp))
          Text("Calendar", style = MaterialTheme.typography.labelLarge)
        }
      }

      if (isPrioritizing) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
      }

      when {
        nextActions.isNotEmpty() -> {
          nextActions.forEachIndexed { index, action ->
            if (index == 0) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            NextActionRow(
              suggestion = action,
              onComplete = { onCompleteAction(action.taskId) },
            )
            if (index < nextActions.lastIndex) {
              HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
          }
        }
        tasks.isEmpty() -> Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
          InlineEmpty("No deadlines to rank", "Add a mock notification with a deadline and the local model will decide what to do first.")
        }
        !isPrioritizing -> Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
          InlineEmpty("No priority plan yet", "The local model has not returned next actions for the current deadlines.")
        }
      }
    }
  }
}

@Composable
private fun NextActionRow(
  suggestion: TaskPrioritySuggestion,
  onComplete: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    UrgencyPill(suggestion.urgency)
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(
        text = suggestion.action,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = suggestion.reason,
        style = MaterialTheme.typography.bodyMedium,
        color = urgencyAccent(suggestion.urgency),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }
    IconButton(onClick = onComplete) {
      Icon(
        Icons.Rounded.CheckCircle,
        contentDescription = "Mark done",
        tint = MaterialTheme.colorScheme.outline,
      )
    }
  }
}

@Composable
private fun UrgencyPill(urgency: String) {
  val (bg, fg) = when (urgency.lowercase(Locale.US)) {
    "now" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    "later" -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
  }
  Surface(color = bg, contentColor = fg, shape = CircleShape, modifier = Modifier.width(68.dp)) {
    Text(
      text = urgency,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = FontWeight.Medium,
      textAlign = TextAlign.Center,
      maxLines = 1,
      modifier = Modifier.padding(vertical = 5.dp),
    )
  }
}

@Composable
private fun urgencyAccent(urgency: String): Color =
  when (urgency.lowercase(Locale.US)) {
    "now" -> MaterialTheme.colorScheme.error
    "later" -> MaterialTheme.colorScheme.outline
    "today" -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
  }

@Composable
private fun InputPanel(
  inputText: String,
  isProcessing: Boolean,
  onInputChange: (String) -> Unit,
  onSubmit: () -> Unit,
) {
  val context = LocalContext.current
  val latestInputText by rememberUpdatedState(inputText)
  val latestOnInputChange by rememberUpdatedState(onInputChange)
  var isListening by remember { mutableStateOf(false) }
  var audioStatus by remember { mutableStateOf("Tap the mic to dictate a mock notification") }
  val speechRecognizer = remember {
    if (SpeechRecognizer.isRecognitionAvailable(context)) {
      SpeechRecognizer.createSpeechRecognizer(context)
    } else {
      null
    }
  }
  val speechIntent = remember {
    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
      putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
      putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a campus notification")
    }
  }
  fun startListening() {
    if (speechRecognizer == null) {
      audioStatus = "Speech recognition is not available on this device"
      return
    }
    isListening = true
    audioStatus = "Listening..."
    speechRecognizer.startListening(speechIntent)
  }
  val microphonePermission = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
  ) { granted ->
    if (granted) {
      startListening()
    } else {
      audioStatus = "Microphone permission denied"
    }
  }

  DisposableEffect(speechRecognizer) {
    speechRecognizer?.setRecognitionListener(object : RecognitionListener {
      override fun onReadyForSpeech(params: Bundle?) {
        audioStatus = "Listening..."
      }

      override fun onBeginningOfSpeech() {
        audioStatus = "Recording voice input"
      }

      override fun onRmsChanged(rmsdB: Float) = Unit
      override fun onBufferReceived(buffer: ByteArray?) = Unit

      override fun onEndOfSpeech() {
        isListening = false
        audioStatus = "Processing voice..."
      }

      override fun onError(error: Int) {
        isListening = false
        audioStatus = speechErrorText(error)
      }

      override fun onResults(results: Bundle?) {
        isListening = false
        val transcript = results
          ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          ?.firstOrNull()
          ?.trim()
          .orEmpty()
        if (transcript.isBlank()) {
          audioStatus = "No speech captured"
        } else {
          val prefix = if (latestInputText.isBlank()) "" else "\n"
          latestOnInputChange("$latestInputText$prefix$transcript")
          audioStatus = "Voice added to mock notification"
        }
      }

      override fun onPartialResults(partialResults: Bundle?) {
        val partial = partialResults
          ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          ?.firstOrNull()
          ?.trim()
        if (!partial.isNullOrBlank()) audioStatus = partial
      }

      override fun onEvent(eventType: Int, params: Bundle?) = Unit
    })

    onDispose {
      speechRecognizer?.destroy()
    }
  }

  CampusCard {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconBadge(icon = Icons.Rounded.Inbox, tint = MaterialTheme.colorScheme.primary)
        Column {
          Text("Chaos Inbox", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
          Text("Manual classifier test", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
      OutlinedTextField(
        value = inputText,
        onValueChange = onInputChange,
        modifier = Modifier.fillMaxWidth().height(150.dp),
        shape = RoundedCornerShape(8.dp),
        placeholder = { Text("Dump your chaotic thoughts here...") },
        trailingIcon = {
          IconButton(
            onClick = {
              if (isListening) {
                speechRecognizer?.stopListening()
                isListening = false
                audioStatus = "Stopped listening"
              } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startListening()
              } else {
                microphonePermission.launch(Manifest.permission.RECORD_AUDIO)
              }
            },
          ) {
            Icon(
              imageVector = if (isListening) Icons.Rounded.MicOff else Icons.Rounded.Mic,
              contentDescription = if (isListening) "Stop voice input" else "Start voice input",
              tint = MaterialTheme.colorScheme.primary,
            )
          }
        },
      )
      Text(
        text = audioStatus,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      Button(
        onClick = onSubmit,
        enabled = !isProcessing,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(8.dp),
      ) {
        Text(if (isProcessing) "Routing locally" else "Run local agent")
        Spacer(Modifier.size(8.dp))
        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(20.dp))
      }
      if (isProcessing) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Deadlines
// ---------------------------------------------------------------------------

@Composable
private fun DeadlinesScreen(
  modifier: Modifier,
  tasks: List<TaskItem>,
  onDeleteTask: (Long) -> Unit,
  onJumpToInbox: () -> Unit,
) {
  val openCount = tasks.count { !it.done }
  var visibleMonth by remember { mutableStateOf(YearMonth.now()) }
  val today = remember { LocalDate.now() }
  val datedTasks = remember(tasks, visibleMonth) {
    tasks.map { task -> DeadlineCalendarEntry(task, parseDeadlineDate(task.dueDateText, visibleMonth)) }
  }
  val tasksByDate = datedTasks.filter { it.date != null }.groupBy { it.date }
  val selectedMonthScheduleCount = datedTasks.count { entry ->
    entry.date?.let { YearMonth.from(it) == visibleMonth } == true
  }
  val unscheduledTasks = datedTasks.filter { it.date == null }.map { it.task }
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    item {
      CenteredHeader(
        title = "Deadline View",
        body = "$openCount open · ${tasks.size} total student deadlines",
      )
    }
    item {
      MonthlyDeadlineCalendar(
        visibleMonth = visibleMonth,
        today = today,
        tasksByDate = tasksByDate,
        scheduleCount = selectedMonthScheduleCount,
        onPreviousMonth = { visibleMonth = visibleMonth.minusMonths(1) },
        onNextMonth = { visibleMonth = visibleMonth.plusMonths(1) },
        onDeleteTask = onDeleteTask,
      )
    }
    if (tasks.isEmpty()) {
      item {
        EmptyState(
          icon = Icons.Rounded.EventBusy,
          title = "No deadlines yet",
          body = "Paste an assignment brief, exam notice, or project reminder and every detected deadline will appear on the calendar.",
          action = "Add deadline text",
          onAction = onJumpToInbox,
        )
      }
    } else if (unscheduledTasks.isNotEmpty()) {
      item {
        Text(
          "Unscheduled",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(start = 4.dp),
        )
      }
      items(unscheduledTasks) { task -> DeadlineCard(task, onDelete = { onDeleteTask(task.id) }) }
    }
  }
}

@Composable
private fun DeadlineCard(task: TaskItem, onDelete: () -> Unit) {
  CampusCard {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconBadge(
        icon = if (task.done) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
        tint = if (task.done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
      )
      Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          task.title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          if (task.source.isNotBlank()) "Source: ${task.source}" else "No source",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      val hasDate = task.dueDateText.isNotBlank()
      Surface(
        color = if (hasDate) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (hasDate) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = CircleShape,
      ) {
        Text(
          text = if (hasDate) task.dueDateText else "Set Date",
          style = MaterialTheme.typography.labelMedium,
          maxLines = 1,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        )
      }
      var menuOpen by remember { mutableStateOf(false) }
      Box {
        IconButton(onClick = { menuOpen = true }) {
          Icon(Icons.Rounded.MoreVert, contentDescription = "More options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
          DropdownMenuItem(
            text = { Text("Delete") },
            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            onClick = {
              menuOpen = false
              onDelete()
            },
          )
        }
      }
    }
  }
}

private data class DeadlineCalendarEntry(
  val task: TaskItem,
  val date: LocalDate?,
)

@Composable
private fun MonthlyDeadlineCalendar(
  visibleMonth: YearMonth,
  today: LocalDate,
  tasksByDate: Map<LocalDate?, List<DeadlineCalendarEntry>>,
  scheduleCount: Int,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onDeleteTask: (Long) -> Unit,
) {
  var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

  CampusCard {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          visibleMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          CircleIconButton(Icons.Rounded.ChevronLeft, "Previous month", onPreviousMonth)
          CircleIconButton(Icons.Rounded.ChevronRight, "Next month", onNextMonth)
        }
      }

      CalendarWeekHeader()
      CalendarMonthGrid(
        visibleMonth = visibleMonth,
        today = today,
        tasksByDate = tasksByDate,
        onDateClick = { date -> selectedDate = date },
      )

      HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape))
        Text(
          "Deadlines · $scheduleCount this month",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }

  selectedDate?.let { date ->
    DeadlineDayDialog(
      date = date,
      tasks = tasksByDate[date].orEmpty().map { it.task },
      onDeleteTask = onDeleteTask,
      onDismiss = { selectedDate = null },
    )
  }
}

@Composable
private fun CircleIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    shape = CircleShape,
    modifier = Modifier.size(36.dp).clickable(onClick = onClick),
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(icon, contentDescription = description, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
  }
}

@Composable
private fun DeadlineDayDialog(
  date: LocalDate,
  tasks: List<TaskItem>,
  onDeleteTask: (Long) -> Unit,
  onDismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("Close") }
    },
    icon = { Icon(Icons.Rounded.Event, contentDescription = null) },
    title = {
      Text(date.format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")))
    },
    text = {
      if (tasks.isEmpty()) {
        Text("No deadlines on this day.")
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            "${tasks.size} deadline${if (tasks.size == 1) "" else "s"}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          tasks.forEach { task ->
            Row(
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalAlignment = Alignment.Top,
            ) {
              Icon(
                imageVector = if (task.done) Icons.Rounded.CheckCircle else Icons.Rounded.Event,
                contentDescription = null,
                tint = if (task.done) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
              )
              Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                  task.title,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                )
                if (task.source.isNotBlank()) {
                  Text(
                    task.source,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
              IconButton(
                onClick = {
                  onDeleteTask(task.id)
                  onDismiss()
                },
                modifier = Modifier.size(36.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.Delete,
                  contentDescription = "Delete deadline",
                  tint = MaterialTheme.colorScheme.error,
                )
              }
            }
          }
        }
      }
    },
  )
}

@Composable
private fun CalendarWeekHeader() {
  val weekdays = listOf("S", "M", "T", "W", "T", "F", "S")
  Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
    weekdays.forEach { day ->
      Text(
        text = day,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun CalendarMonthGrid(
  visibleMonth: YearMonth,
  today: LocalDate,
  tasksByDate: Map<LocalDate?, List<DeadlineCalendarEntry>>,
  onDateClick: (LocalDate) -> Unit,
) {
  val firstOfMonth = visibleMonth.atDay(1)
  // Sunday-first grid (Sun=0 .. Sat=6).
  val leadOffset = firstOfMonth.dayOfWeek.value % 7
  val firstGridDate = firstOfMonth.minusDays(leadOffset.toLong())
  val weeks = List(6) { week ->
    List(7) { day -> firstGridDate.plusDays((week * 7 + day).toLong()) }
  }

  Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
    weeks.forEach { week ->
      Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
        week.forEach { date ->
          CalendarDayCell(
            date = date,
            visibleMonth = visibleMonth,
            today = today,
            entries = tasksByDate[date].orEmpty(),
            onClick = { onDateClick(date) },
            modifier = Modifier.weight(1f),
          )
        }
      }
    }
  }
}

@Composable
private fun CalendarDayCell(
  date: LocalDate,
  visibleMonth: YearMonth,
  today: LocalDate,
  entries: List<DeadlineCalendarEntry>,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val inMonth = YearMonth.from(date) == visibleMonth
  val isToday = date == today
  val hasEntries = entries.isNotEmpty()

  val circleColor = when {
    isToday -> MaterialTheme.colorScheme.primary
    hasEntries && inMonth -> CampusPalette.surfaceContainerHigh
    else -> Color.Transparent
  }
  val dayColor = when {
    isToday -> MaterialTheme.colorScheme.onPrimary
    !inMonth -> MaterialTheme.colorScheme.outlineVariant
    hasEntries -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurface
  }

  Box(
    modifier = modifier
      .aspectRatio(1f)
      .then(if (hasEntries) Modifier.clickable(onClick = onClick) else Modifier),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier.size(36.dp).background(circleColor, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = date.dayOfMonth.toString(),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = if (isToday || hasEntries) FontWeight.Bold else FontWeight.Normal,
        color = dayColor,
      )
    }
    if (hasEntries) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .size(16.dp)
          .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = entries.size.toString(),
          color = MaterialTheme.colorScheme.onSecondaryContainer,
          fontSize = 9.sp,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Study
// ---------------------------------------------------------------------------

@Composable
private fun StudyScreen(
  modifier: Modifier,
  flashcards: List<Flashcard>,
  expenses: List<ExpenseItem>,
  onJumpToInbox: () -> Unit,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
  ) {
    item {
      CampusCard {
        Row(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconBadge(icon = Icons.Rounded.School, tint = MaterialTheme.colorScheme.primary)
          Column {
            Text("Study Items", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(
              "${flashcards.size} flashcards · ${expenses.size} expenses",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
    if (flashcards.isEmpty() && expenses.isEmpty()) {
      item {
        EmptyState(
          icon = Icons.Rounded.School,
          title = "Nothing sorted yet",
          body = "Run one inbox item and parsed flashcards or spending notes will show up here.",
          action = "Open inbox",
          onAction = onJumpToInbox,
        )
      }
    } else {
      item {
        SectionHeader(
          icon = Icons.Rounded.Style,
          title = "Flashcards",
          badgeColor = MaterialTheme.colorScheme.tertiaryContainer,
          onBadgeColor = MaterialTheme.colorScheme.onTertiaryContainer,
        )
      }
      if (flashcards.isEmpty()) {
        item { InfoCard("No flashcards yet", "Lecture notes become quick revision cards here.") }
      }
      items(flashcards) { card -> FlashcardCard(card) }

      item {
        SectionHeader(
          icon = Icons.AutoMirrored.Rounded.ReceiptLong,
          title = "Expenses",
          badgeColor = MaterialTheme.colorScheme.errorContainer,
          onBadgeColor = MaterialTheme.colorScheme.onErrorContainer,
        )
      }
      if (expenses.isEmpty()) {
        item { InfoCard("No expenses yet", "Receipt and payment details are grouped here.") }
      }
      items(expenses) { expense -> ExpenseCard(expense) }
    }
  }
}

@Composable
private fun FlashcardCard(card: Flashcard) {
  CampusCard {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Box(
        modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(Icons.Rounded.Style, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(20.dp))
      }
      Text(card.front, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
      HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
      Text(card.back, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      if (card.source.isNotBlank()) {
        Surface(
          color = CampusPalette.surfaceContainer,
          contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
          shape = RoundedCornerShape(6.dp),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Icon(Icons.Rounded.AutoStories, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(card.source, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
          }
        }
      }
    }
  }
}

@Composable
private fun ExpenseCard(expense: ExpenseItem) {
  CampusCard {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconBadge(icon = Icons.Rounded.CreditCard, tint = MaterialTheme.colorScheme.secondary)
      Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            expense.merchant.ifBlank { "Expense" },
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
          )
          Text(
            expense.amountText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          if (expense.category.isNotBlank()) {
            Surface(
              color = MaterialTheme.colorScheme.secondaryContainer,
              contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
              shape = CircleShape,
            ) {
              Text(
                expense.category,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
              )
            }
          }
          if (expense.source.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              Icon(Icons.Rounded.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
              Text(
                expense.source,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Runtime
// ---------------------------------------------------------------------------

@Composable
private fun ModelScreen(
  modifier: Modifier,
  modelName: String,
  modelId: String,
  modelFile: String,
  commitHash: String,
  sizeInBytes: Long,
  modelPath: String,
  backend: String,
  maxTokens: Int,
  modelDownloadState: ModelDownloadState,
  onSavePath: (String) -> Unit,
  onDownloadModel: (String) -> Unit,
) {
  var draftPath by remember(modelPath) { mutableStateOf(modelPath) }
  val endpoint = remember(modelId, commitHash, modelFile) {
    "https://huggingface.co/$modelId/resolve/$commitHash/$modelFile?download=true"
  }
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    item {
      Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        IconBadge(icon = Icons.Rounded.Memory, tint = MaterialTheme.colorScheme.primary)
        Text("AI Runtime", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(
          "One local model powers routing",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
    item {
      RuntimeCard(
        modelName = modelName,
        modelFile = modelFile,
        sizeInBytes = sizeInBytes,
        modelPath = modelPath,
        backend = backend,
        maxTokens = maxTokens,
      )
    }
    if (modelDownloadState.isDownloading || modelDownloadState.progressPercent != null) {
      item { DownloadProgress(modelDownloadState) }
    }
    item { EndpointCard(endpoint) }
    item {
      CampusCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
          Text("Local model path", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          OutlinedTextField(
            value = draftPath,
            onValueChange = { draftPath = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            placeholder = { Text("/storage/emulated/0/Android/data/.../$modelFile") },
            supportingText = { Text("Paste a downloaded model path if the download manager is not used.") },
            minLines = 2,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
              onClick = { onDownloadModel(endpoint) },
              enabled = !modelDownloadState.isDownloading,
              modifier = Modifier.weight(1f).height(48.dp),
              shape = RoundedCornerShape(8.dp),
            ) {
              Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(20.dp))
              Spacer(Modifier.size(8.dp))
              Text(if (modelDownloadState.isDownloading) "Downloading" else "Download")
            }
            OutlinedButton(
              onClick = { onSavePath(draftPath) },
              modifier = Modifier.weight(1f).height(48.dp),
              shape = RoundedCornerShape(8.dp),
            ) {
              Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(20.dp))
              Spacer(Modifier.size(8.dp))
              Text("Save path")
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RuntimeCard(
  modelName: String,
  modelFile: String,
  sizeInBytes: Long,
  modelPath: String,
  backend: String,
  maxTokens: Int,
) {
  CampusCard {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon = Icons.Rounded.SmartToy, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f)) {
          Text(modelName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text(
            "$modelFile · ${sizeInBytes / (1024 * 1024)} MB",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NeutralPill("LiteRT-LM")
        NeutralPill(backend)
        NeutralPill("$maxTokens tokens")
        val saved = modelPath.isNotBlank()
        Surface(
          color = if (saved) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer,
          contentColor = if (saved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onErrorContainer,
          shape = CircleShape,
        ) {
          Text(
            if (saved) "Path saved" else "Path missing",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun NeutralPill(text: String) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor = MaterialTheme.colorScheme.onSurface,
    shape = CircleShape,
  ) {
    Text(
      text,
      style = MaterialTheme.typography.labelMedium,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
    )
  }
}

@Composable
private fun DownloadProgress(modelDownloadState: ModelDownloadState) {
  Card(
    colors = CardDefaults.cardColors(containerColor = CampusPalette.secondaryFixed),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text("Download manager", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = CampusPalette.onSecondaryFixed)
      Text(modelDownloadState.statusText, style = MaterialTheme.typography.labelLarge, color = CampusPalette.onSecondaryFixedVariant)
      modelDownloadState.progressPercent?.let {
        LinearProgressIndicator(
          progress = { it / 100f },
          color = MaterialTheme.colorScheme.secondary,
          trackColor = Color.White.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth(),
        )
        Text("$it% complete", style = MaterialTheme.typography.labelMedium, color = CampusPalette.onSecondaryFixedVariant)
      }
      if (modelDownloadState.downloadedPath.isNotBlank()) {
        Text(
          modelDownloadState.downloadedPath,
          style = MaterialTheme.typography.labelMedium,
          color = CampusPalette.onSecondaryFixedVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun EndpointCard(endpoint: String) {
  CampusCard {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
          modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Icon(Icons.Rounded.DataObject, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Text("Hugging Face endpoint", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
      }
      Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
      ) {
        Text(
          endpoint,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Clip,
          modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 10.dp),
        )
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Shared components
// ---------------------------------------------------------------------------

@Composable
private fun CampusCard(content: @Composable () -> Unit) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    content()
  }
}

@Composable
private fun HeroPanel(icon: ImageVector, title: String, body: String) {
  Surface(
    color = MaterialTheme.colorScheme.primaryContainer,
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(20.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier = Modifier.size(42.dp).background(Color.White, CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(24.dp))
      }
      Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, color = Color.White)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
      }
    }
  }
}

@Composable
private fun StatTile(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
  CampusCardModifier(modifier) {
    Column(
      Modifier.padding(16.dp).fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      IconBadge(icon = icon, tint = MaterialTheme.colorScheme.primary)
      Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
      Text(
        label.uppercase(Locale.US),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun CampusCardModifier(modifier: Modifier, content: @Composable () -> Unit) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier,
  ) {
    content()
  }
}

@Composable
private fun CenteredHeader(title: String, body: String) {
  Column(
    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
    Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, badgeColor: Color, onBadgeColor: Color) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.padding(start = 4.dp),
  ) {
    Box(
      modifier = Modifier.size(32.dp).background(badgeColor, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(icon, contentDescription = null, tint = onBadgeColor, modifier = Modifier.size(16.dp))
    }
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
  }
}

@Composable
private fun EmptyState(icon: ImageVector, title: String, body: String, action: String, onAction: () -> Unit) {
  CampusCard {
    Column(
      modifier = Modifier.fillMaxWidth().padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      IconBadge(icon = icon, tint = MaterialTheme.colorScheme.secondary)
      Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
      Text(
        body,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
      Button(onClick = onAction, shape = RoundedCornerShape(8.dp)) { Text(action) }
    }
  }
}

@Composable
private fun InfoCard(title: String, body: String) {
  CampusCard {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
      Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun InlineEmpty(title: String, body: String) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun IconBadge(icon: ImageVector, tint: Color) {
  Box(
    modifier = Modifier
      .size(42.dp)
      .background(tint.copy(alpha = 0.12f), CircleShape),
    contentAlignment = Alignment.Center,
  ) {
    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
  }
}

@Composable
private fun StatusPill(text: String, ready: Boolean, modifier: Modifier = Modifier) {
  Surface(
    color = if (ready) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
    contentColor = if (ready) Color.White else MaterialTheme.colorScheme.onErrorContainer,
    shape = CircleShape,
    modifier = modifier,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Icon(Icons.Rounded.CloudDone, contentDescription = null, modifier = Modifier.size(16.dp))
      Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
  }
}

private fun speechErrorText(error: Int): String =
  when (error) {
    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
    SpeechRecognizer.ERROR_CLIENT -> "Voice input stopped"
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
    SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition"
    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Speech recognition timed out"
    SpeechRecognizer.ERROR_NO_MATCH -> "No speech match found"
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy"
    SpeechRecognizer.ERROR_SERVER -> "Speech service error"
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard"
    else -> "Voice input failed"
  }

// ---------------------------------------------------------------------------
// Date parsing for the deadline calendar
// ---------------------------------------------------------------------------

private fun parseDeadlineDate(text: String, visibleMonth: YearMonth): LocalDate? {
  val value = text.trim()
  if (value.isBlank()) return null

  parseIsoDate(value)?.let { return it }
  parseSlashDate(value, visibleMonth)?.let { return it }
  parseMonthNameDate(value, visibleMonth)?.let { return it }
  parseRelativeDate(value)?.let { return it }

  return null
}

private fun parseIsoDate(value: String): LocalDate? {
  val match = Regex("""\b(\d{4})-(\d{1,2})-(\d{1,2})\b""").find(value) ?: return null
  return localDateOrNull(
    year = match.groupValues[1].toInt(),
    month = match.groupValues[2].toInt(),
    day = match.groupValues[3].toInt(),
  )
}

private fun parseSlashDate(value: String, visibleMonth: YearMonth): LocalDate? {
  val match = Regex("""\b(\d{1,2})[/-](\d{1,2})(?:[/-](\d{2,4}))?\b""").find(value) ?: return null
  val day = match.groupValues[1].toInt()
  val month = match.groupValues[2].toInt()
  val yearText = match.groupValues[3]
  val year = when {
    yearText.isBlank() -> visibleMonth.year
    yearText.length == 2 -> 2000 + yearText.toInt()
    else -> yearText.toInt()
  }
  return localDateOrNull(year = year, month = month, day = day)
}

private fun parseMonthNameDate(value: String, visibleMonth: YearMonth): LocalDate? {
  val monthPattern = "(jan|january|feb|february|mar|march|apr|april|may|jun|june|jul|july|aug|august|sep|sept|september|oct|october|nov|november|dec|december)"
  val lower = value.lowercase(Locale.US)
  Regex("""\b$monthPattern\s+(\d{1,2})(?:,?\s+(\d{4}))?\b""").find(lower)?.let { match ->
    val month = monthNumber(match.groupValues[1]) ?: return null
    val day = match.groupValues[2].toInt()
    val year = match.groupValues[3].takeIf { it.isNotBlank() }?.toInt() ?: visibleMonth.year
    return localDateOrNull(year = year, month = month, day = day)
  }
  Regex("""\b(\d{1,2})\s+$monthPattern(?:,?\s+(\d{4}))?\b""").find(lower)?.let { match ->
    val day = match.groupValues[1].toInt()
    val month = monthNumber(match.groupValues[2]) ?: return null
    val year = match.groupValues[3].takeIf { it.isNotBlank() }?.toInt() ?: visibleMonth.year
    return localDateOrNull(year = year, month = month, day = day)
  }
  return null
}

private fun parseRelativeDate(value: String): LocalDate? {
  val lower = value.lowercase(Locale.US)
  val today = LocalDate.now()
  if (Regex("""\btoday\b""").containsMatchIn(lower)) return today
  if (Regex("""\btomorrow\b""").containsMatchIn(lower)) return today.plusDays(1)

  val weekdays = mapOf(
    "monday" to DayOfWeek.MONDAY,
    "tuesday" to DayOfWeek.TUESDAY,
    "wednesday" to DayOfWeek.WEDNESDAY,
    "thursday" to DayOfWeek.THURSDAY,
    "friday" to DayOfWeek.FRIDAY,
    "saturday" to DayOfWeek.SATURDAY,
    "sunday" to DayOfWeek.SUNDAY,
  )
  weekdays.forEach { (name, dayOfWeek) ->
    if (Regex("""\b$name\b""").containsMatchIn(lower)) {
      return today.with(TemporalAdjusters.nextOrSame(dayOfWeek))
    }
  }
  return null
}

private fun monthNumber(value: String): Int? =
  when (value.take(3).lowercase(Locale.US)) {
    "jan" -> 1
    "feb" -> 2
    "mar" -> 3
    "apr" -> 4
    "may" -> 5
    "jun" -> 6
    "jul" -> 7
    "aug" -> 8
    "sep" -> 9
    "oct" -> 10
    "nov" -> 11
    "dec" -> 12
    else -> null
  }

private fun localDateOrNull(year: Int, month: Int, day: Int): LocalDate? =
  runCatching { LocalDate.of(year, month, day) }.getOrNull()
