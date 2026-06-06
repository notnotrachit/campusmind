package com.campusmind.app.ai

import android.content.Context
import com.campusmind.app.model.ModelConfig
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.SamplerConfig
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.withContext

private const val MAX_LITERT_TOKENS = 128

class LiteRtLocalLlmEngine(
  private val context: Context,
) : LocalLlmEngine {
  override var isReady: Boolean = false
    private set

  private var activeModelPath: String = ""
  private var activeMaxTokens: Int = 0
  private var engine: Engine? = null

  override suspend fun initialize(config: ModelConfig): Result<Unit> {
    val modelPath = config.modelPath
    val maxTokens = config.maxTokens.coerceIn(64, MAX_LITERT_TOKENS)
    if (isReady && activeModelPath == modelPath && activeMaxTokens == maxTokens && engine != null) return Result.success(Unit)

    return withContext(Dispatchers.IO) {
      runCatching {
        require(modelPath.isNotBlank()) { "Download the Hugging Face model before running agents" }
        require(File(modelPath).exists()) { "Downloaded model file does not exist: $modelPath" }

        close()
        val selectedBackend = Backend.CPU()
        val newEngine = Engine(
          EngineConfig(
            modelPath = modelPath,
            backend = selectedBackend,
            visionBackend = null,
            audioBackend = null,
            maxNumTokens = maxTokens,
            maxNumImages = null,
            cacheDir = context.getExternalFilesDir(null)?.absolutePath,
          ),
        )
        newEngine.initialize()

        engine = newEngine
        activeModelPath = modelPath
        activeMaxTokens = maxTokens
        isReady = true
      }.onFailure {
        isReady = false
      }
    }
  }

  override suspend fun generate(prompt: String, options: GenerationOptions): Result<String> =
    withContext(Dispatchers.IO) {
      runCatching {
        val activeEngine = checkNotNull(engine) { "LiteRT-LM has not been initialized" }
        var activeConversation: Conversation? = null
        try {
          activeConversation = activeEngine.createConversation(
            ConversationConfig(
              samplerConfig = SamplerConfig(
                topK = options.topK.coerceIn(1, 40),
                topP = options.topP.coerceIn(0f, 1f).toDouble(),
                temperature = options.temperature.coerceIn(0f, 1f).toDouble(),
              ),
            ),
          )
          val message = activeConversation.sendMessageAsync(Contents.of(prompt)).last()
          message.toString()
        } finally {
          activeConversation?.close()
        }
      }.onFailure {
        close()
      }
    }

  fun close() {
    engine?.close()
    engine = null
    activeModelPath = ""
    activeMaxTokens = 0
    isReady = false
  }

}
