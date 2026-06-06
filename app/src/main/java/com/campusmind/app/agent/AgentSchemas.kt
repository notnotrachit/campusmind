package com.campusmind.app.agent

import com.campusmind.app.ai.StructuredSchema

/**
 * Structured-output schemas for each agent. The local model is constrained to call the matching
 * tool, so its reply is always a JSON object with these exact fields instead of free-form text.
 */
object AgentSchemas {
  val deadline = StructuredSchema(
    toolName = "save_deadlines",
    systemInstruction = "You extract student deadlines. Always call save_deadlines with at least one task. Return dueDateText as an absolute ISO date in yyyy-MM-dd format.",
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
                  "dueDateText": { "type": "string", "description": "Absolute due date in yyyy-MM-dd format. Resolve relative dates using the current date/time in the prompt." }
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

  val notification = StructuredSchema(
    toolName = "classify_student_notification",
    systemInstruction = """
      You classify phone notifications for a student. Always call classify_student_notification.
      Mark important true only when the notification contains a deadline, class/exam/project action,
      study material worth revising, academic admin work, or student spending. Ignore ads, social likes,
      generic app status, OTPs, and casual messages unless they include student work.
      Return every task dueDateText as an absolute ISO date in yyyy-MM-dd format.
    """.trimIndent(),
    toolDescriptionJson = """
      {
        "name": "classify_student_notification",
        "description": "Classify a phone notification and save all student-actionable items.",
        "parameters": {
          "type": "object",
          "properties": {
            "important": { "type": "boolean", "description": "Whether this notification matters for student productivity." },
            "category": { "type": "string", "enum": ["deadline", "flashcard", "expense", "mixed", "ignore"] },
            "summary": { "type": "string", "description": "One short sentence explaining the classification." },
            "tasks": {
              "type": "array",
              "description": "Deadlines, reminders, exams, submissions, applications, or actions to track.",
              "items": {
                "type": "object",
                "properties": {
                  "title": { "type": "string", "description": "Short, specific action title." },
                  "dueDateText": { "type": "string", "description": "Absolute due date in yyyy-MM-dd format. Resolve relative dates using the current date/time in the prompt. Use a reasonable yyyy-MM-dd estimate only if important but no date is stated." }
                },
                "required": ["title", "dueDateText"]
              }
            },
            "flashcards": {
              "type": "array",
              "description": "Study facts or concepts worth revising.",
              "items": {
                "type": "object",
                "properties": {
                  "front": { "type": "string", "description": "Question for revision." },
                  "back": { "type": "string", "description": "Answer for revision." }
                },
                "required": ["front", "back"]
              }
            },
            "expenses": {
              "type": "array",
              "description": "Student spending found in payment or receipt notifications.",
              "items": {
                "type": "object",
                "properties": {
                  "amountText": { "type": "string", "description": "Amount with currency." },
                  "category": { "type": "string", "enum": ["Food", "Travel", "Academics", "Student spend"] },
                  "merchant": { "type": "string", "description": "Merchant or recipient." }
                },
                "required": ["amountText", "category", "merchant"]
              }
            }
          },
          "required": ["important", "category", "summary", "tasks", "flashcards", "expenses"]
        }
      }
    """.trimIndent(),
  )

  val nextActions = StructuredSchema(
    toolName = "prioritize_student_tasks",
    systemInstruction = """
      You are a student planning assistant. Always call prioritize_student_tasks.
      Rank the student's next tasks using urgency, academic impact, effort, and due dates.
      Return only actions that the student can do next, ordered from highest priority to lowest.
    """.trimIndent(),
    toolDescriptionJson = """
      {
        "name": "prioritize_student_tasks",
        "description": "Prioritize upcoming student deadlines into concrete next actions.",
        "parameters": {
          "type": "object",
          "properties": {
            "summary": { "type": "string", "description": "One short sentence summarizing the plan." },
            "nextActions": {
              "type": "array",
              "description": "The top next actions to do now.",
              "items": {
                "type": "object",
                "properties": {
                  "taskId": { "type": "integer", "description": "The id of the existing task this action belongs to." },
                  "action": { "type": "string", "description": "Specific next action the student should do." },
                  "reason": { "type": "string", "description": "Why this action is prioritized." },
                  "urgency": { "type": "string", "enum": ["Now", "Today", "Next", "Later"] }
                },
                "required": ["taskId", "action", "reason", "urgency"]
              }
            }
          },
          "required": ["summary", "nextActions"]
        }
      }
    """.trimIndent(),
  )
}
