package com.campusmind.app.ai

import com.campusmind.app.model.ModelConfig
import com.campusmind.app.model.RuntimeType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class RuntimeOrchestratorTest {
  @Test
  fun choosesAiCoreWhenReady() = runBlocking {
    val orchestrator = orchestrator(
      aiCore = FakeEngine(RuntimeType.AICORE, response = """{"summary":"ok"}"""),
      liteRt = FakeEngine(RuntimeType.LITERT_LM, response = "lite"),
      stub = FakeEngine(RuntimeType.STUB, response = "stub"),
    )

    val result = orchestrator.generate("prompt").getOrThrow()

    assertEquals(RuntimeType.AICORE, result.runtimeType)
  }

  @Test
  fun fallsBackToLiteRtWhenAiCoreUnavailable() = runBlocking {
    val orchestrator = orchestrator(
      config = ModelConfig(modelPath = "/sdcard/Download/model.task"),
      aiCore = FakeEngine(RuntimeType.AICORE, initFailure = "no ai core"),
      liteRt = FakeEngine(RuntimeType.LITERT_LM, response = "lite"),
      stub = FakeEngine(RuntimeType.STUB, response = "stub"),
    )

    val result = orchestrator.generate("prompt").getOrThrow()

    assertEquals(RuntimeType.LITERT_LM, result.runtimeType)
  }

  @Test
  fun fallsBackToStubWhenRealRuntimesFail() = runBlocking {
    val orchestrator = orchestrator(
      config = ModelConfig(modelPath = "/sdcard/Download/model.task"),
      aiCore = FakeEngine(RuntimeType.AICORE, initFailure = "no ai core"),
      liteRt = FakeEngine(RuntimeType.LITERT_LM, generateFailure = "bad model"),
      stub = FakeEngine(RuntimeType.STUB, response = "stub"),
    )

    val result = orchestrator.generate("prompt").getOrThrow()

    assertEquals(RuntimeType.STUB, result.runtimeType)
  }

  private fun orchestrator(
    config: ModelConfig = ModelConfig(),
    aiCore: LocalLlmEngine,
    liteRt: LocalLlmEngine,
    stub: LocalLlmEngine,
  ) = RuntimeOrchestrator(
    configProvider = { config },
    aiCoreEngine = aiCore,
    liteRtEngine = liteRt,
    stubEngine = stub,
  )

  private class FakeEngine(
    override val runtimeType: RuntimeType,
    private val response: String = "",
    private val initFailure: String = "",
    private val generateFailure: String = "",
  ) : LocalLlmEngine {
    override var isReady: Boolean = false
      private set

    override suspend fun initialize(config: ModelConfig): Result<Unit> {
      if (initFailure.isNotBlank()) return Result.failure(IllegalStateException(initFailure))
      isReady = true
      return Result.success(Unit)
    }

    override suspend fun generate(prompt: String, options: GenerationOptions): Result<String> =
      if (generateFailure.isNotBlank()) {
        Result.failure(IllegalStateException(generateFailure))
      } else {
        Result.success(response)
      }
  }
}
