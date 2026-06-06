package com.campusmind.app.agent

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/**
 * Turns a free-text due phrase (or the raw task text) into a concrete calendar date.
 *
 * Every deadline gets a real date: explicit phrases like "tomorrow", "by 15 March", or
 * "12/06" are resolved against today, and anything without a detectable date falls back to
 * a sensible default so the task always lands on the calendar instead of sitting in limbo.
 * The output is formatted so the calendar parser in the UI can re-read it reliably.
 */
object DueDateResolver {
  private const val DEFAULT_DAYS_AHEAD = 7L
  private val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.US)

  private const val MONTH_PATTERN =
    "(jan|january|feb|february|mar|march|apr|april|may|jun|june|jul|july|aug|august|sep|sept|september|oct|october|nov|november|dec|december)"

  private val weekdays = mapOf(
    "monday" to DayOfWeek.MONDAY,
    "tuesday" to DayOfWeek.TUESDAY,
    "wednesday" to DayOfWeek.WEDNESDAY,
    "thursday" to DayOfWeek.THURSDAY,
    "friday" to DayOfWeek.FRIDAY,
    "saturday" to DayOfWeek.SATURDAY,
    "sunday" to DayOfWeek.SUNDAY,
  )

  /** Resolve to a formatted, UI-parseable date string, never blank. */
  fun resolveText(rawPhrase: String?, fallbackText: String = "", today: LocalDate = LocalDate.now()): String =
    formatter.format(resolveDate(rawPhrase, fallbackText, today))

  /** Resolve to a concrete date, defaulting to one week out when nothing is detectable. */
  fun resolveDate(rawPhrase: String?, fallbackText: String = "", today: LocalDate = LocalDate.now()): LocalDate =
    listOfNotNull(rawPhrase, fallbackText)
      .firstNotNullOfOrNull { parse(it, today) }
      ?: today.plusDays(DEFAULT_DAYS_AHEAD)

  /** Best-effort parse of a single phrase into a date, or null if none is present. */
  fun parse(text: String?, today: LocalDate = LocalDate.now()): LocalDate? {
    val value = text?.trim().orEmpty()
    if (value.isBlank()) return null
    return parseIso(value)
      ?: parseSlash(value, today)
      ?: parseMonthName(value, today)
      ?: parseRelative(value, today)
  }

  private fun parseIso(value: String): LocalDate? {
    val match = Regex("""\b(\d{4})-(\d{1,2})-(\d{1,2})\b""").find(value) ?: return null
    return localDateOrNull(match.groupValues[1].toInt(), match.groupValues[2].toInt(), match.groupValues[3].toInt())
  }

  private fun parseSlash(value: String, today: LocalDate): LocalDate? {
    val match = Regex("""\b(\d{1,2})[/-](\d{1,2})(?:[/-](\d{2,4}))?\b""").find(value) ?: return null
    val day = match.groupValues[1].toInt()
    val month = match.groupValues[2].toInt()
    val yearText = match.groupValues[3]
    val hasYear = yearText.isNotBlank()
    val year = when {
      !hasYear -> today.year
      yearText.length == 2 -> 2000 + yearText.toInt()
      else -> yearText.toInt()
    }
    return localDateOrNull(year, month, day)?.let { if (!hasYear) bumpIfPast(it, today) else it }
  }

  private fun parseMonthName(value: String, today: LocalDate): LocalDate? {
    val lower = value.lowercase(Locale.US)
    // "june 19", "june 19th", "june 19th 2026"
    Regex("""\b$MONTH_PATTERN\s+(\d{1,2})(?:st|nd|rd|th)?(?:,?\s+(\d{4}))?\b""").find(lower)?.let { match ->
      val month = monthNumber(match.groupValues[1]) ?: return null
      val day = match.groupValues[2].toInt()
      val yearText = match.groupValues[3]
      return resolveMonthDay(day, month, yearText, today)
    }
    // "19 june", "19th june", "19th june 2026"
    Regex("""\b(\d{1,2})(?:st|nd|rd|th)?\s+$MONTH_PATTERN(?:,?\s+(\d{4}))?\b""").find(lower)?.let { match ->
      val day = match.groupValues[1].toInt()
      val month = monthNumber(match.groupValues[2]) ?: return null
      val yearText = match.groupValues[3]
      return resolveMonthDay(day, month, yearText, today)
    }
    return null
  }

  private fun resolveMonthDay(day: Int, month: Int, yearText: String, today: LocalDate): LocalDate? {
    val hasYear = yearText.isNotBlank()
    val year = if (hasYear) yearText.toInt() else today.year
    return localDateOrNull(year, month, day)?.let { if (!hasYear) bumpIfPast(it, today) else it }
  }

  private fun parseRelative(value: String, today: LocalDate): LocalDate? {
    val lower = value.lowercase(Locale.US)
    if (Regex("""\b(today|tonight)\b""").containsMatchIn(lower)) return today
    if (Regex("""\btomorrow\b""").containsMatchIn(lower)) return today.plusDays(1)
    if (Regex("""\bday after tomorrow\b""").containsMatchIn(lower)) return today.plusDays(2)

    Regex("""\bin\s+(\d{1,2})\s+days?\b""").find(lower)?.let { return today.plusDays(it.groupValues[1].toLong()) }
    Regex("""\bin\s+(\d{1,2})\s+weeks?\b""").find(lower)?.let { return today.plusWeeks(it.groupValues[1].toLong()) }

    val nextWeek = Regex("""\bnext\s+week\b""").containsMatchIn(lower)
    if (nextWeek && weekdays.keys.none { lower.contains(it) }) {
      return today.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
    }

    weekdays.forEach { (name, dayOfWeek) ->
      if (Regex("""\b$name\b""").containsMatchIn(lower)) {
        val adjuster = if (Regex("""\bnext\s+$name\b""").containsMatchIn(lower)) {
          TemporalAdjusters.next(dayOfWeek)
        } else {
          TemporalAdjusters.nextOrSame(dayOfWeek)
        }
        return today.with(adjuster)
      }
    }
    return null
  }

  /** A bare month/day (no year) that already passed this year means next year's occurrence. */
  private fun bumpIfPast(date: LocalDate, today: LocalDate): LocalDate =
    if (date.isBefore(today)) date.plusYears(1) else date

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
}
