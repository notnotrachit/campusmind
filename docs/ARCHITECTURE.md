# CampusMind Architecture

CampusMind is a local-first Android app. The phone captures messy student inputs, stores them locally, and routes extracted text through small agents. On-device LLM execution is isolated behind `LocalLlmEngine` so the first app can run with deterministic stubs while Google AI Edge LiteRT LM wiring is added incrementally.

## Subsystems

- **Chaos Inbox:** receives text, screenshots, notes, receipts, and future voice/camera captures.
- **Agent Router:** chooses Study, Deadline, or Expense based on input text.
- **Local Agents:** produce flashcards, tasks, expenses, and activity-log entries.
- **Storage:** Room keeps demo data local and inspectable.
- **Model Runtime:** manual model path plus LiteRT LM integration point. The local Google AI Edge Gallery clone is the reference for engine initialization and backend selection.
- **Office Kit Bridge:** future optional layer for green-light laptop-assisted processing; not required for the first phone-only demo.

## First Runtime Strategy

The first push uses fake agent output behind production-shaped interfaces. This keeps UI, persistence, and demo flows unblocked while the team validates the correct LiteRT model format and device performance on iQOO hardware.
