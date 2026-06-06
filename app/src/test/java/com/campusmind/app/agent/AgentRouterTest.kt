package com.campusmind.app.agent

import com.campusmind.app.model.AgentKind
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentRouterTest {
  private val router = AgentRouter()

  @Test
  fun routesAssignmentTextToDeadlineAgent() {
    assertEquals(
      AgentKind.Deadline,
      router.chooseKind("Submit DBMS assignment by Friday"),
    )
  }

  @Test
  fun routesReceiptTextToExpenseAgent() {
    assertEquals(
      AgentKind.Expense,
      router.chooseKind("Canteen receipt total Rs 120 paid by UPI"),
    )
  }

  @Test
  fun routesLectureTextToStudyAgent() {
    assertEquals(
      AgentKind.Study,
      router.chooseKind("Operating systems manage processes memory and files"),
    )
  }

  @Test
  fun deadlineAgentProducesTask() = runBlocking {
    val result = router.route("Submit DBMS assignment by Friday")
    assertEquals(AgentKind.Deadline, result.kind)
    assertTrue(result.tasks.isNotEmpty())
  }

  @Test
  fun studyAgentProducesFlashcards() = runBlocking {
    val result = router.route("Operating systems schedule processes and manage memory")
    assertEquals(AgentKind.Study, result.kind)
    assertTrue(result.flashcards.isNotEmpty())
  }

  @Test
  fun expenseAgentProducesExpense() = runBlocking {
    val result = router.route("Canteen receipt total Rs 120 paid by UPI")
    assertEquals(AgentKind.Expense, result.kind)
    assertTrue(result.expenses.isNotEmpty())
  }
}
