package com.campusmind.app.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class MlKitOcrEngine(private val context: Context) : OcrEngine {
  override suspend fun extractText(imageUri: Uri): Result<String> =
    runCatching {
      val image = InputImage.fromFilePath(context, imageUri)
      val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
      suspendCancellableCoroutine { continuation ->
        recognizer.process(image)
          .addOnSuccessListener { result -> continuation.resume(result.text) }
          .addOnFailureListener { error -> continuation.resumeWith(Result.failure(error)) }
      }
    }
}
