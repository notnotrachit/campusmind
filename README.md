# CampusMind

CampusMind is a phone-first student productivity assistant for the AgentKit sprint. The app treats the phone as the primary interface for screenshots, notes, receipts, and reminders, then routes the captured text through local student-life agents.

## MVP

- Native Android app built with Kotlin and Jetpack Compose.
- Chaos Inbox for student inputs.
- Three starter agents: Study, Deadline, and Expense.
- Local Room database for inbox history, tasks, flashcards, expenses, and activity logs.
- One on-device LiteRT-LM model: `Gemma-4-E2B-it`.
- In-app download from the same Hugging Face resolve endpoint shape used by Google AI Edge Gallery.

## Setup

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

The app does not bundle the model. Open AI Runtime and download `Gemma-4-E2B-it`; CampusMind stores the `.litertlm` file in app-specific external storage and uses that single model for all agents.

## Hackathon Positioning

CampusMind is designed for iQOO phone-first judging: useful behavior runs from the Android app, and future laptop/cloud work through iQOO Office Kit is optional polish rather than a hard dependency.
