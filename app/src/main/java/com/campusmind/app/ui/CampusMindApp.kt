package com.campusmind.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DataObject
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.campusmind.app.ai.ModelDownloadState
import com.campusmind.app.model.ActivityLog
import com.campusmind.app.model.ExpenseItem
import com.campusmind.app.model.Flashcard
import com.campusmind.app.model.TaskItem
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

private enum class Tab(val label: String, val icon: ImageVector) {
  Inbox("Inbox", Icons.Rounded.Inbox),
  Deadlines("Deadlines", Icons.Rounded.Event),
  Study("Study", Icons.Rounded.School),
  Model("Runtime", Icons.Rounded.Memory),
  Activity("Activity", Icons.Rounded.History),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusMindApp(viewModel: CampusMindViewModel) {
  val state by viewModel.uiState.collectAsState()
  var selectedTab by remember { mutableStateOf(Tab.Inbox) }

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
            Text("CampusMind", fontWeight = FontWeight.Bold)
            Text(
              text = state.status,
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        },
        actions = {
          RuntimePill(
            text = if (state.modelConfig.modelPath.isBlank()) "Model needed" else "Offline ready",
            ready = state.modelConfig.modelPath.isNotBlank(),
          )
        },
      )
    },
    bottomBar = {
      NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        Tab.entries.forEach { tab ->
          NavigationBarItem(
            selected = selectedTab == tab,
            onClick = { selectedTab = tab },
            icon = { Icon(tab.icon, contentDescription = tab.label) },
            label = { Text(tab.label) },
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
        deadlineCount = state.tasks.size,
        flashcardCount = state.flashcards.size,
        expenseCount = state.expenses.size,
        onInputChange = viewModel::updateInput,
        onSubmit = viewModel::submitInput,
        onJumpToDeadlines = { selectedTab = Tab.Deadlines },
      )
      Tab.Deadlines -> DeadlinesScreen(
        modifier = contentModifier,
        tasks = state.tasks,
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
      Tab.Activity -> ActivityScreen(
        modifier = contentModifier,
        logs = state.logs,
        onJumpToInbox = { selectedTab = Tab.Inbox },
      )
    }
  }
}

@Composable
private fun InboxScreen(
  modifier: Modifier,
  inputText: String,
  isProcessing: Boolean,
  deadlineCount: Int,
  flashcardCount: Int,
  expenseCount: Int,
  onInputChange: (String) -> Unit,
  onSubmit: () -> Unit,
  onJumpToDeadlines: () -> Unit,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      HeroPanel(
        title = "Turn campus chaos into next actions.",
        body = "Paste a notice, receipt, assignment brief, or lecture note. The local agent sorts it into deadlines, flashcards, and expenses on this device.",
        trailing = {
          IconBadge(icon = Icons.Rounded.Bolt, tint = MaterialTheme.colorScheme.primary)
        },
      )
    }
    item {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        StatTile("Deadlines", deadlineCount.toString(), Icons.Rounded.Event, Modifier.weight(1f))
        StatTile("Cards", flashcardCount.toString(), Icons.Rounded.Style, Modifier.weight(1f))
        StatTile("Spend", expenseCount.toString(), Icons.AutoMirrored.Rounded.ReceiptLong, Modifier.weight(1f))
      }
    }
    item {
      InputPanel(
        inputText = inputText,
        isProcessing = isProcessing,
        onInputChange = onInputChange,
        onSubmit = onSubmit,
      )
    }
    item {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        FilterChip(selected = true, onClick = {}, label = { Text("Text") }, leadingIcon = { Icon(Icons.Rounded.CheckCircle, null) })
        FilterChip(selected = false, onClick = {}, label = { Text("Camera soon") })
        FilterChip(selected = false, onClick = {}, label = { Text("Voice soon") })
      }
    }
    item {
      TextButton(onClick = onJumpToDeadlines, modifier = Modifier.fillMaxWidth()) {
        Text("Open deadline view")
      }
    }
  }
}

@Composable
private fun DeadlinesScreen(
  modifier: Modifier,
  tasks: List<TaskItem>,
  onJumpToInbox: () -> Unit,
) {
  val openCount = tasks.count { !it.done }
  var visibleMonth by remember { mutableStateOf(YearMonth.now()) }
  val today = remember { LocalDate.now() }
  val datedTasks = remember(tasks, visibleMonth) {
    tasks.map { task -> DeadlineCalendarEntry(task, parseDeadlineDate(task.dueDateText, visibleMonth)) }
  }
  val tasksByDate = datedTasks.filter { it.date != null }.groupBy { it.date }
  val unscheduledTasks = datedTasks.filter { it.date == null }.map { it.task }
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      ScreenHeader(
        icon = Icons.Rounded.Event,
        title = "Deadline View",
        body = "$openCount open · ${tasks.size} total student deadlines",
      )
    }
    if (tasks.isEmpty()) {
      item {
        EmptyState(
          title = "No deadlines yet",
          body = "Paste an assignment brief, exam notice, or project reminder and every detected deadline will appear here.",
          action = "Add deadline text",
          onAction = onJumpToInbox,
        )
      }
    } else {
      item {
        MonthlyDeadlineCalendar(
          visibleMonth = visibleMonth,
          today = today,
          tasksByDate = tasksByDate,
          onPreviousMonth = { visibleMonth = visibleMonth.minusMonths(1) },
          onNextMonth = { visibleMonth = visibleMonth.plusMonths(1) },
        )
      }
      if (unscheduledTasks.isNotEmpty()) {
        item { SectionTitle("Needs Date", unscheduledTasks.size, Icons.Rounded.Event) }
        items(unscheduledTasks) { task -> DeadlineCard(task) }
      }
    }
  }
}

@Composable
private fun StudyScreen(
  modifier: Modifier,
  flashcards: List<Flashcard>,
  expenses: List<ExpenseItem>,
  onJumpToInbox: () -> Unit,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      ScreenHeader(
        icon = Icons.Rounded.School,
        title = "Study Items",
        body = "${flashcards.size} flashcards · ${expenses.size} expenses",
      )
    }
    if (flashcards.isEmpty() && expenses.isEmpty()) {
      item {
        EmptyState(
          title = "Nothing sorted yet",
          body = "Run one inbox item and parsed flashcards or spending notes will show up here.",
          action = "Open inbox",
          onAction = onJumpToInbox,
        )
      }
    } else {
      item { SectionTitle("Flashcards", flashcards.size, Icons.Rounded.Style) }
      if (flashcards.isEmpty()) item { EmptyCard("No flashcards", "Lecture notes become quick revision cards here.") }
      items(flashcards) { card -> ResultCard(card.front, card.back, Icons.Rounded.School, card.source) }

      item { SectionTitle("Expenses", expenses.size, Icons.AutoMirrored.Rounded.ReceiptLong) }
      if (expenses.isEmpty()) item { EmptyCard("No expenses", "Receipt and payment details are grouped here.") }
      items(expenses) { expense ->
        ResultCard("${expense.merchant} · ${expense.amountText}", expense.category, Icons.Rounded.CreditCard, expense.source)
      }
    }
  }
}

@Composable
private fun DeadlineCard(task: TaskItem) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
      ) {
        IconBadge(
          icon = if (task.done) Icons.Rounded.CheckCircle else Icons.Rounded.Event,
          tint = if (task.done) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
          RuntimePill(task.dueDateText.ifBlank { "Due date pending" }, ready = !task.done)
        }
      }
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
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onPreviousMonth) {
          Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous month")
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            visibleMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
          Text(
            "${tasksByDate.values.sumOf { it.size }} scheduled",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        IconButton(onClick = onNextMonth) {
          Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next month")
        }
      }

      CalendarWeekHeader()
      CalendarMonthGrid(
        visibleMonth = visibleMonth,
        today = today,
        tasksByDate = tasksByDate,
      )
    }
  }
}

@Composable
private fun CalendarWeekHeader() {
  val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
  Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
    weekdays.forEach { day ->
      Text(
        text = day,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
) {
  val firstOfMonth = visibleMonth.atDay(1)
  val firstGridDate = firstOfMonth.minusDays((firstOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
  val weeks = List(6) { week ->
    List(7) { day -> firstGridDate.plusDays((week * 7 + day).toLong()) }
  }

  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    weeks.forEach { week ->
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
        week.forEach { date ->
          CalendarDayCell(
            date = date,
            visibleMonth = visibleMonth,
            today = today,
            entries = tasksByDate[date].orEmpty(),
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
  modifier: Modifier = Modifier,
) {
  val inMonth = YearMonth.from(date) == visibleMonth
  val isToday = date == today
  val backgroundColor = when {
    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    entries.isNotEmpty() -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (inMonth) 0.52f else 0.24f)
  }
  val dayColor = when {
    !inMonth -> MaterialTheme.colorScheme.outline.copy(alpha = 0.58f)
    isToday -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurface
  }

  Surface(
    color = backgroundColor,
    shape = RoundedCornerShape(8.dp),
    modifier = modifier.aspectRatio(0.78f),
  ) {
    Column(Modifier.padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text(
        text = date.dayOfMonth.toString(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold,
        color = dayColor,
      )
      entries.take(2).forEach { entry ->
        CalendarTaskChip(entry.task)
      }
      if (entries.size > 2) {
        Text(
          "+${entries.size - 2}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Composable
private fun CalendarTaskChip(task: TaskItem) {
  Surface(
    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    contentColor = MaterialTheme.colorScheme.primary,
    shape = RoundedCornerShape(6.dp),
  ) {
    Text(
      text = task.title,
      style = MaterialTheme.typography.labelSmall,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
    )
  }
}

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
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      ScreenHeader(
        icon = Icons.Rounded.Memory,
        title = "AI Runtime",
        body = "One local LiteRT-LM model powers routing for the hackathon MVP.",
      )
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
      item {
        DownloadProgress(modelDownloadState)
      }
    }
    item {
      Button(
        onClick = { onDownloadModel(endpoint) },
        enabled = !modelDownloadState.isDownloading,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Icon(Icons.Rounded.Download, contentDescription = null)
        Spacer(Modifier.size(8.dp))
        Text(if (modelDownloadState.isDownloading) "Downloading model" else "Download model")
      }
    }
    item {
      OutlinedTextField(
        value = draftPath,
        onValueChange = { draftPath = it },
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Local model path") },
        placeholder = { Text("/storage/emulated/0/Android/data/.../$modelFile") },
        supportingText = { Text("Paste a downloaded model path if the download manager is not used.") },
        minLines = 2,
      )
    }
    item {
      OutlinedButton(onClick = { onSavePath(draftPath) }, modifier = Modifier.fillMaxWidth()) {
        Icon(Icons.Rounded.Settings, contentDescription = null)
        Spacer(Modifier.size(8.dp))
        Text("Save path")
      }
    }
    item { DetailCard("Hugging Face endpoint", endpoint, Icons.Rounded.DataObject) }
  }
}

@Composable
private fun ActivityScreen(
  modifier: Modifier,
  logs: List<ActivityLog>,
  onJumpToInbox: () -> Unit,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      ScreenHeader(
        icon = Icons.Rounded.History,
        title = "Local Activity",
        body = "${logs.size} stored agent actions",
      )
    }
    if (logs.isEmpty()) {
      item {
        EmptyState(
          title = "No local actions yet",
          body = "Activity appears after the first agent run.",
          action = "Run an item",
          onAction = onJumpToInbox,
        )
      }
    } else {
      items(logs) { log -> ResultCard(log.message, "Stored on device", Icons.Rounded.History) }
    }
  }
}

@Composable
private fun HeroPanel(
  title: String,
  body: String,
  trailing: @Composable () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(18.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(body, style = MaterialTheme.typography.bodyMedium)
      }
      trailing()
    }
  }
}

@Composable
private fun InputPanel(
  inputText: String,
  isProcessing: Boolean,
  onInputChange: (String) -> Unit,
  onSubmit: () -> Unit,
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        IconBadge(icon = Icons.Rounded.Inbox, tint = MaterialTheme.colorScheme.tertiary)
        Column {
          Text("Chaos Inbox", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
          Text("Paste raw campus text", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
      OutlinedTextField(
        value = inputText,
        onValueChange = onInputChange,
        modifier = Modifier.fillMaxWidth().height(190.dp),
        label = { Text("Student input") },
        placeholder = { Text("Submit DBMS assignment by Friday, split mess bill, revise OS paging...") },
      )
      Button(
        onClick = onSubmit,
        enabled = !isProcessing,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null)
        Spacer(Modifier.size(8.dp))
        Text(if (isProcessing) "Routing locally" else "Run local agent")
      }
      if (isProcessing) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
      }
    }
  }
}

@Composable
private fun StatTile(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(8.dp),
    modifier = modifier,
  ) {
    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
      Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
  }
}

@Composable
private fun ScreenHeader(icon: ImageVector, title: String, body: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    IconBadge(icon = icon, tint = MaterialTheme.colorScheme.primary)
    Column {
      Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
      Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
private fun SectionTitle(text: String, count: Int, icon: ImageVector) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
      Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
    Text("$count", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

@Composable
private fun EmptyState(title: String, body: String, action: String, onAction: () -> Unit) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant,
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      IconBadge(icon = Icons.Rounded.Inbox, tint = MaterialTheme.colorScheme.secondary)
      Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
      Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Button(onClick = onAction) { Text(action) }
    }
  }
}

@Composable
private fun EmptyCard(title: String, body: String) {
  DetailCard(title, body, Icons.Rounded.CheckCircle)
}

@Composable
private fun ResultCard(title: String, body: String, icon: ImageVector, source: String? = null) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      IconBadge(icon = icon, tint = MaterialTheme.colorScheme.tertiary)
      Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (!source.isNullOrBlank()) {
          Text(
            source,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

@Composable
private fun DetailCard(title: String, body: String, icon: ImageVector) {
  ResultCard(title, body, icon)
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
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconBadge(icon = Icons.Rounded.Memory, tint = MaterialTheme.colorScheme.primary)
        Column(Modifier.weight(1f)) {
          Text(modelName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          Text("$modelFile · ${sizeInBytes / (1024 * 1024)} MB", style = MaterialTheme.typography.bodyMedium)
        }
      }
      FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RuntimePill("LiteRT-LM", ready = true)
        RuntimePill(backend, ready = true)
        RuntimePill("$maxTokens tokens", ready = true)
        RuntimePill(if (modelPath.isBlank()) "Path missing" else "Path saved", ready = modelPath.isNotBlank())
      }
    }
  }
}

@Composable
private fun DownloadProgress(modelDownloadState: ModelDownloadState) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    shape = RoundedCornerShape(8.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text("Download manager", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
      Text(modelDownloadState.statusText, style = MaterialTheme.typography.bodyMedium)
      modelDownloadState.progressPercent?.let {
        LinearProgressIndicator(progress = { it / 100f }, modifier = Modifier.fillMaxWidth())
        Text("$it% complete", style = MaterialTheme.typography.labelMedium)
      }
      if (modelDownloadState.downloadedPath.isNotBlank()) {
        Text(modelDownloadState.downloadedPath, style = MaterialTheme.typography.bodySmall)
      }
    }
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
    Icon(icon, contentDescription = null, tint = tint)
  }
}

@Composable
private fun RuntimePill(text: String, ready: Boolean) {
  Surface(
    color = if (ready) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
    contentColor = if (ready) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
    shape = RoundedCornerShape(8.dp),
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelMedium,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
