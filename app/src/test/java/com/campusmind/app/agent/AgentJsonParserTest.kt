package com.campusmind.app.agent

import com.campusmind.app.ai.GenerationOptions
import com.campusmind.app.ai.LocalLlmEngine
import com.campusmind.app.ai.RuntimeOrchestrator
import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.ModelConfig
import com.campusmind.app.model.RuntimeType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentJsonParserTest {
  @Test
  fun parsesDeadlineTasksFromJson() {
    val result = AgentJsonParser.parse(
      kind = AgentKind.Deadline,
      rawText = """{"summary":"Task found","tasks":[{"title":"Submit DBMS","dueDateText":"Friday"}]}""",
      sourceText = "Submit DBMS assignment by Friday",
      runtimeType = RuntimeType.AICORE,
      runtimeStatusText = "AI Core active",
    )

    assertEquals("Submit DBMS", result?.tasks?.first()?.title)
    assertEquals(RuntimeType.AICORE, result?.runtimeType)
  }

  @Test
  fun parsesStudyFlashcardsFromJson() {
    val result = AgentJsonParser.parse(
      kind = AgentKind.Study,
      rawText = """{"summary":"Cards","flashcards":[{"front":"What is OS?","back":"System software"}]}""",
      sourceText = "Operating systems manage resources",
      runtimeType = RuntimeType.AICORE,
      runtimeStatusText = "AI Core active",
    )

    assertEquals("What is OS?", result?.flashcards?.first()?.front)
  }

  @Test
  fun parsesExpensesFromJson() {
    val result = AgentJsonParser.parse(
      kind = AgentKind.Expense,
      rawText = """{"summary":"Spend","expenses":[{"amountText":"Rs 120","category":"Food","merchant":"Canteen"}]}""",
      sourceText = "Canteen receipt total Rs 120",
      runtimeType = RuntimeType.AICORE,
      runtimeStatusText = "AI Core active",
    )

    assertEquals("Rs 120", result?.expenses?.first()?.amountText)
  }

  @Test
  fun malformedLlmResponseFallsBackToDeterministicDeadlineOutput() = runBlocking {
    val runtimeOrchestrator = RuntimeOrchestrator(
      configProvider = { ModelConfig() },
      aiCoreEngine = FakeEngine(RuntimeType.AICORE, "not json"),
      liteRtEngine = FakeEngine(RuntimeType.LITERT_LM, "not json"),
      stubEngine = FakeEngine(RuntimeType.STUB, "not json"),
    )
    val result = DeadlineAgent(runtimeOrchestrator).analyze("Submit DBMS assignment by Friday")

    assertEquals(RuntimeType.STUB, result.runtimeType)
    assertTrue(result.tasks.isNotEmpty())
  }

  private class FakeEngine(
    override val runtimeType: RuntimeType,
    private val response: String,
  ) : LocalLlmEngine {
    override var isReady: Boolean = false
      private set

    override suspend fun initialize(config: ModelConfig): Result<Unit> {
      isReady = true
      return Result.success(Unit)
    }

    override suspend fun generate(prompt: String, options: GenerationOptions): Result<String> =
      Result.success(response)
  }
}
