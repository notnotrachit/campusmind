package com.campusmind.app.agent

import com.campusmind.app.ai.RuntimeOrchestrator
import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.AgentResult

class AgentRouter(
  runtimeOrchestrator: RuntimeOrchestrator? = null,
  private val studyAgent: StudentAgent = StudyAgent(runtimeOrchestrator),
  private val deadlineAgent: StudentAgent = DeadlineAgent(runtimeOrchestrator),
  private val expenseAgent: StudentAgent = ExpenseAgent(runtimeOrchestrator),
) {
  suspend fun route(inputText: String): AgentResult {
    val kind = chooseKind(inputText)
    return when (kind) {
      AgentKind.Study -> studyAgent.analyze(inputText)
      AgentKind.Deadline -> deadlineAgent.analyze(inputText)
      AgentKind.Expense -> expenseAgent.analyze(inputText)
    }
  }

  fun chooseKind(inputText: String): AgentKind {
    val lower = inputText.lowercase()
    return when {
      listOf("rs", "inr", "₹", "upi", "paid", "receipt", "canteen", "total").any(lower::contains) -> AgentKind.Expense
      listOf("submit", "deadline", "due", "assignment", "exam", "remind").any(lower::contains) -> AgentKind.Deadline
      else -> AgentKind.Study
    }
  }
}
