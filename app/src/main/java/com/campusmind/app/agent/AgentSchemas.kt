package com.campusmind.app.agent

import com.campusmind.app.ai.StructuredSchema

/**
 * Structured-output schemas for each agent. The local model is constrained to call the matching
 * tool, so its reply is always a JSON object with these exact fields instead of free-form text.
 */
object AgentSchemas {
  val deadline = StructuredSchema(
    toolName = "save_deadlines",
    systemInstruction = "You extract student deadlines. Always call save_deadlines with at least one task.",
    toolDescriptionJson = """
      {
        "name": "save_deadlines",
        "description": "Record the deadlines or tasks found in the student's note.",
        "parameters": {
          "type": "object",
          "properties": {
            "summary": { "type": "string", "description": "One short sentence describing what was found." },
            "tasks": {
              "type": "array",
              "description": "Each deadline or task to track.",
              "items": {
                "type": "object",
                "properties": {
                  "title": { "type": "string", "description": "Short, specific name of the work to do." },
                  "dueDateText": { "type": "string", "description": "When it is due, e.g. tomorrow, Friday, 15 March. Use 'this week' if none is stated." }
                },
                "required": ["title", "dueDateText"]
              }
            }
          },
          "required": ["summary", "tasks"]
        }
      }
    """.trimIndent(),
  )

  val study = StructuredSchema(
    toolName = "save_flashcards",
    systemInstruction = "You turn study notes into revision flashcards. Always call save_flashcards.",
    toolDescriptionJson = """
      {
        "name": "save_flashcards",
        "description": "Turn the student's note into concise revision flashcards.",
        "parameters": {
          "type": "object",
          "properties": {
            "summary": { "type": "string", "description": "One short sentence describing the note." },
            "flashcards": {
              "type": "array",
              "description": "Two concise question/answer cards.",
              "items": {
                "type": "object",
                "properties": {
                  "front": { "type": "string", "description": "The question." },
                  "back": { "type": "string", "description": "The answer." }
                },
                "required": ["front", "back"]
              }
            }
          },
          "required": ["summary", "flashcards"]
        }
      }
    """.trimIndent(),
  )

  val expense = StructuredSchema(
    toolName = "save_expenses",
    systemInstruction = "You log student spending. Always call save_expenses with one expense.",
    toolDescriptionJson = """
      {
        "name": "save_expenses",
        "description": "Record the spending found in the student's note.",
        "parameters": {
          "type": "object",
          "properties": {
            "summary": { "type": "string", "description": "One short sentence describing the spend." },
            "expenses": {
              "type": "array",
              "description": "Each expense found.",
              "items": {
                "type": "object",
                "properties": {
                  "amountText": { "type": "string", "description": "Amount with currency, e.g. ₹120." },
                  "category": { "type": "string", "enum": ["Food", "Travel", "Academics", "Student spend"] },
                  "merchant": { "type": "string", "description": "Where it was spent." }
                },
                "required": ["amountText", "category", "merchant"]
              }
            }
          },
          "required": ["summary", "expenses"]
        }
      }
    """.trimIndent(),
  )
}
