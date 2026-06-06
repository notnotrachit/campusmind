package com.campusmind.app.agent

import com.campusmind.app.ai.SingleModelRunner
import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.AgentResult

class AgentRouter(
  modelRunner: SingleModelRunner,
  private val studyAgent: StudentAgent = StudyAgent(modelRunner),
  private val deadlineAgent: StudentAgent = DeadlineAgent(modelRunner),
  private val expenseAgent: StudentAgent = ExpenseAgent(modelRunner),
) {
  suspend fun route(inputText: String): AgentResult {
    AgentFallbacks.structured(inputText)?.let { return it }

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
