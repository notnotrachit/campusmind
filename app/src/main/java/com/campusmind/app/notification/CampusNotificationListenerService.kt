package com.campusmind.app.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.campusmind.app.CampusMindApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class CampusNotificationListenerService : NotificationListenerService() {
  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun onNotificationPosted(sbn: StatusBarNotification) {
    if (sbn.packageName == packageName) return

    val content = sbn.toStudentSignalText()
    if (content.isBlank()) return

    val app = application as CampusMindApplication
    serviceScope.launch {
      runCatching {
        app.repository.submitNotification(
          notificationKey = sbn.stableCampusKey(),
          content = content,
        )
      }
    }
  }

  override fun onDestroy() {
    serviceScope.cancel()
    super.onDestroy()
  }

  private fun StatusBarNotification.stableCampusKey(): String =
    listOf(packageName, tag.orEmpty(), id.toString(), postTime.toString()).joinToString(":")

  private fun StatusBarNotification.toStudentSignalText(): String {
    val extras = notification.extras
    val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
    val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
    val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()
    val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
    val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
      ?.mapNotNull { it?.toString() }
      .orEmpty()

    return buildList {
      add("Package: $packageName")
      addIfNotBlank("Title", title)
      addIfNotBlank("Text", text)
      addIfNotBlank("Subtext", subText)
      addIfNotBlank("Expanded", bigText)
      lines.forEach { addIfNotBlank("Line", it) }
    }
      .distinct()
      .joinToString("\n")
      .take(2_000)
  }

  private fun MutableList<String>.addIfNotBlank(label: String, value: String) {
    val clean = value.trim()
    if (clean.isNotBlank()) add("$label: $clean")
  }
}
