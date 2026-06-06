package com.campusmind.app.ocr

import android.net.Uri

interface OcrEngine {
  suspend fun extractText(imageUri: Uri): Result<String>
}
