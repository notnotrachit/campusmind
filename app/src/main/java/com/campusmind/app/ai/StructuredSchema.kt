package com.campusmind.app.ai

/**
 * Describes the structured output an agent expects from the local model.
 *
 * The model is given [toolDescriptionJson] as a callable tool (OpenAPI / function-declaration
 * format with a top-level "name", "description", and "parameters") and is constrained to reply
 * with a tool call whose arguments match the schema. The engine returns those arguments as a JSON
 * object string, so callers always get well-formed JSON instead of best-effort free text.
 */
data class StructuredSchema(
  val toolName: String,
  val systemInstruction: String,
  val toolDescriptionJson: String,
)
