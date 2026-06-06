package com.campusmind.app.agent

import com.campusmind.app.model.AgentKind
import com.campusmind.app.model.AgentResult

interface StudentAgent {
  val kind: AgentKind
  suspend fun analyze(inputText: String): AgentResult
}
