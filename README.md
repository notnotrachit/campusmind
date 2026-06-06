# CampusMind

CampusMind is a phone-first student productivity assistant for the AgentKit sprint. The app treats the phone as the primary interface for screenshots, notes, receipts, and reminders, then routes the captured text through local student-life agents.

## MVP

- Native Android app built with Kotlin and Jetpack Compose.
- Chaos Inbox for student inputs.
- Three starter agents: Study, Deadline, and Expense.
- Local Room database for inbox history, tasks, flashcards, expenses, and activity logs.
- Manual local model configuration for Google AI Edge LiteRT LM.
- Stubbed agent responses first, with interfaces ready for on-device LLM execution.

## Setup

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

The first scaffold does not bundle a model. Use the Model screen to record a local model path on the device once a LiteRT-compatible model is available.

## Hackathon Positioning

CampusMind is designed for iQOO phone-first judging: useful behavior runs from the Android app, and future laptop/cloud work through iQOO Office Kit is optional polish rather than a hard dependency.
