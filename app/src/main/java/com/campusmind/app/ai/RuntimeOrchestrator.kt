package com.campusmind.app.ai

import com.campusmind.app.model.ModelConfig
import com.campusmind.app.model.RuntimeType
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RuntimeGenerationResult(
  val text: String,
  val runtimeType: RuntimeType,
  val statusText: String,
)

data class RuntimeState(
  val activeRuntime: RuntimeType = RuntimeType.STUB,
  val statusText: String = "Runtime not initialized",
  val lastFailureReason: String = "",
)

class RuntimeOrchestrator(
  private val configProvider: suspend () -> ModelConfig,
  private val aiCoreEngine: LocalLlmEngine = AiCoreLocalLlmEngine(),
  private val liteRtEngine: LocalLlmEngine,
  private val stubEngine: LocalLlmEngine = StubLocalLlmEngine(),
) {
  private val mutableState = MutableStateFlow(RuntimeState())
  val state: StateFlow<RuntimeState> = mutableState.asStateFlow()

  suspend fun generate(prompt: String): Result<RuntimeGenerationResult> {
    val config = configProvider()
    val options = GenerationOptions(
      maxTokens = config.maxOutputTokens,
      temperature = config.temperature.coerceIn(0f, 1f),
      topK = config.topK,
    )

    val aiCoreFailure = tryEngine(
      engine = aiCoreEngine,
      config = config,
      options = options,
      prompt = prompt,
      statusText = "AI Core active",
    )
    if (aiCoreFailure.isSuccess) return aiCoreFailure

    val liteRtFailure =
      if (config.modelPath.isNotBlank()) {
        tryEngine(
          engine = liteRtEngine,
          config = config,
          options = options,
          prompt = prompt,
          statusText = "LiteRT LM fallback active",
          previousFailure = aiCoreFailure.exceptionOrNull()?.message.orEmpty(),
        )
      } else {
        Result.failure(IllegalStateException("LiteRT model path is empty"))
      }
    if (liteRtFailure.isSuccess) return liteRtFailure

    val combinedFailure = listOfNotNull(
      aiCoreFailure.exceptionOrNull()?.message,
      liteRtFailure.exceptionOrNull()?.message,
    ).joinToString(" | ")

    return tryEngine(
      engine = stubEngine,
      config = config,
      options = options,
      prompt = prompt,
      statusText = "Stub fallback active",
      previousFailure = combinedFailure,
    )
  }

  private suspend fun tryEngine(
    engine: LocalLlmEngine,
    config: ModelConfig,
    options: GenerationOptions,
    prompt: String,
    statusText: String,
    previousFailure: String = "",
  ): Result<RuntimeGenerationResult> = coroutineScope {
    val statusJob =
      if (engine is AiCoreLocalLlmEngine) {
        launch {
          engine.status.collect { status ->
            mutableState.value = RuntimeState(engine.runtimeType, status, previousFailure)
          }
        }
      } else {
        null
      }

    val initResult = engine.initialize(config)
    statusJob?.cancelAndJoin()
    if (initResult.isFailure) {
      val reason = initResult.exceptionOrNull()?.message ?: "Initialization failed"
      mutableState.value = RuntimeState(engine.runtimeType, "$statusText unavailable", reason)
      return@coroutineScope Result.failure(IllegalStateException(reason))
    }

    val generateResult = engine.generate(prompt, options)
    if (generateResult.isFailure) {
      val reason = generateResult.exceptionOrNull()?.message ?: "Generation failed"
      mutableState.value = RuntimeState(engine.runtimeType, "$statusText failed", reason)
      return@coroutineScope Result.failure(IllegalStateException(reason))
    }

    val selectedStatus =
      if (previousFailure.isBlank()) statusText else "$statusText; previous fallback reason: $previousFailure"
    mutableState.value = RuntimeState(engine.runtimeType, selectedStatus, previousFailure)
    return@coroutineScope Result.success(
      RuntimeGenerationResult(
        text = generateResult.getOrThrow(),
        runtimeType = engine.runtimeType,
        statusText = selectedStatus,
      ),
    )
  }
}
