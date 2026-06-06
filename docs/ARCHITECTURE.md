# CampusMind Architecture

CampusMind is a local-first Android app. The phone captures messy student inputs, stores them locally, and routes extracted text through small agents. On-device LLM execution is isolated behind `LocalLlmEngine` and uses one downloaded LiteRT-LM model.

## Subsystems

- **Chaos Inbox:** receives text, screenshots, notes, receipts, and future voice/camera captures.
- **Agent Router:** chooses Study, Deadline, or Expense based on input text.
- **Local Agents:** prompt the single LiteRT-LM model for JSON and persist parsed flashcards, tasks, expenses, and activity-log entries.
- **Storage:** Room keeps demo data local and inspectable.
- **Model Runtime:** `Gemma-4-E2B-it` downloaded from the same Hugging Face resolve endpoint shape used by Google AI Edge Gallery, then loaded through LiteRT-LM `Engine` and `Conversation`.
- **Office Kit Bridge:** future optional layer for green-light laptop-assisted processing; not required for the first phone-only demo.

## Runtime Strategy

CampusMind uses one model only: `litert-community/gemma-4-E2B-it-litert-lm` / `gemma-4-E2B-it.litertlm` at commit `6e5c4f1e395deb959c494953478fa5cec4b8008f`. There is no AI Core path and no deterministic fallback. If the model is missing, generation fails, or the model returns malformed JSON, the run reports an error instead of creating synthetic entities.
