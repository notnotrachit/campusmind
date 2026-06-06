package com.campusmind.app.ai

import android.content.Context
import com.campusmind.app.model.ModelConfig
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.ExperimentalApi
import com.google.ai.edge.litertlm.ExperimentalFlags
import com.google.ai.edge.litertlm.OpenApiTool
import com.google.ai.edge.litertlm.SamplerConfig
import com.google.ai.edge.litertlm.tool
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

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
            ConversationConfig(samplerConfig = options.toSamplerConfig()),
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

  @OptIn(ExperimentalApi::class)
  override suspend fun generateStructured(
    prompt: String,
    schema: StructuredSchema,
    options: GenerationOptions,
  ): Result<String> =
    withContext(Dispatchers.IO) {
      runCatching {
        val activeEngine = checkNotNull(engine) { "LiteRT-LM has not been initialized" }
        // Constrain decoding so the model can only emit a valid call to the schema's tool.
        ExperimentalFlags.enableConversationConstrainedDecoding = true
        var activeConversation: Conversation? = null
        try {
          activeConversation = activeEngine.createConversation(
            ConversationConfig(
              systemInstruction = Contents.of(schema.systemInstruction),
              tools = listOf(tool(SchemaTool(schema))),
              samplerConfig = options.toSamplerConfig(),
              automaticToolCalling = false,
            ),
          )
          val message = activeConversation.sendMessageAsync(Contents.of(prompt)).last()
          val call = message.toolCalls.firstOrNull { it.name == schema.toolName }
            ?: message.toolCalls.firstOrNull()
          // Structured arguments map -> JSON; fall back to raw text if no tool call was made.
          call?.let { it.arguments.toJsonElement().toString() } ?: message.toString()
        } finally {
          activeConversation?.close()
        }
      }.onFailure {
        close()
      }
    }

  private fun GenerationOptions.toSamplerConfig(): SamplerConfig =
    SamplerConfig(
      topK = topK.coerceIn(1, 40),
      topP = topP.coerceIn(0f, 1f).toDouble(),
      temperature = temperature.coerceIn(0f, 1f).toDouble(),
    )

  /** Presents a [StructuredSchema] to LiteRT-LM as a callable tool. */
  private class SchemaTool(private val schema: StructuredSchema) : OpenApiTool {
    override fun getToolDescriptionJsonString(): String = schema.toolDescriptionJson

    // Never invoked: automaticToolCalling is disabled, so we read the call instead of running it.
    override fun execute(params: String): String = "{}"
  }

  fun close() {
    engine?.close()
    engine = null
    activeModelPath = ""
    activeMaxTokens = 0
    isReady = false
  }
}

/** Serialize a tool-call argument value (Map / List / primitive) into JSON for the parser. */
private fun Any?.toJsonElement(): JsonElement = when (this) {
  null -> JsonNull
  is JsonElement -> this
  is Boolean -> JsonPrimitive(this)
  is Number -> JsonPrimitive(this)
  is String -> JsonPrimitive(this)
  is Map<*, *> -> JsonObject(entries.associate { (key, value) -> key.toString() to value.toJsonElement() })
  is Iterable<*> -> JsonArray(map { it.toJsonElement() })
  is Array<*> -> JsonArray(map { it.toJsonElement() })
  else -> JsonPrimitive(toString())
}
