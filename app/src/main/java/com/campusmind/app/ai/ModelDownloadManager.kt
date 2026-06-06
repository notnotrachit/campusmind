package com.campusmind.app.ai

import android.content.Context
import android.os.Environment
import com.campusmind.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

private val LEGACY_MODEL_FILES = listOf("gemma3-1b-it-int4.litertlm")

data class ModelDownloadState(
  val isDownloading: Boolean = false,
  val progressPercent: Int? = null,
  val statusText: String = "No model download running",
  val downloadedPath: String = "",
)

class ModelDownloadManager(
  private val context: Context,
) {
  private val mutableState = MutableStateFlow(ModelDownloadState())
  val state: StateFlow<ModelDownloadState> = mutableState.asStateFlow()

  init {
    deleteLegacyModels()
  }

  suspend fun download(urlText: String, fileName: String): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
      deleteLegacyModels()
      val url = URL(urlText.trim())
      val safeFileName = fileName.ifBlank { url.file.substringAfterLast('/').substringBefore('?') }
        .ifBlank { "campusmind-model.litertlm" }
      val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
      val targetFile = File(targetDir, safeFileName)
      val tempFile = File(targetDir, "$safeFileName.part")

      mutableState.value = ModelDownloadState(isDownloading = true, statusText = "Connecting to model URL")

      val connection = (url.openConnection() as HttpURLConnection).apply {
        connectTimeout = 15_000
        readTimeout = 60_000
        instanceFollowRedirects = true
        val token = context.getString(R.string.hugging_face_token)
        if (token.isNotBlank()) {
          setRequestProperty("Authorization", "Bearer $token")
        }
      }

      try {
        connection.connect()
        val responseCode = connection.responseCode
        require(responseCode in 200..299) { "Download failed with HTTP $responseCode" }

        val totalBytes = connection.contentLengthLong.takeIf { it > 0 }
        connection.inputStream.use { input ->
          tempFile.outputStream().use { output ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var downloadedBytes = 0L
            while (true) {
              val read = input.read(buffer)
              if (read < 0) break
              output.write(buffer, 0, read)
              downloadedBytes += read
              val percent = totalBytes?.let { ((downloadedBytes * 100) / it).toInt().coerceIn(0, 100) }
              mutableState.value = ModelDownloadState(
                isDownloading = true,
                progressPercent = percent,
                statusText = percent?.let { "Downloading model: $it%" } ?: "Downloading model",
              )
            }
          }
        }

        if (targetFile.exists()) targetFile.delete()
        require(tempFile.renameTo(targetFile)) { "Could not save downloaded model" }
        mutableState.value = ModelDownloadState(
          isDownloading = false,
          progressPercent = 100,
          statusText = "Model downloaded",
          downloadedPath = targetFile.absolutePath,
        )
        targetFile.absolutePath
      } finally {
        connection.disconnect()
        if (tempFile.exists() && !targetFile.exists()) tempFile.delete()
      }
    }.onFailure { error ->
      mutableState.value = ModelDownloadState(
        isDownloading = false,
        statusText = "Model download failed: ${error.message ?: error::class.java.simpleName}",
      )
    }
  }

  private fun deleteLegacyModels() {
    val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
    LEGACY_MODEL_FILES.forEach { fileName ->
      File(targetDir, fileName).takeIf { it.exists() }?.delete()
    }
  }
}
