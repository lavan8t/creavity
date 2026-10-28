# Ticket 05: Synchronized Timed Quizzes & Live Poll Engine (Mentimeter Style)

Status: resolved
Completed At: 2026-10-28T12:17:30+05:30
Blocked by: 01

## What it delivers
- Data models for interactive assessments:
  - `QuizType` (`POLL`, `TIMED_QUIZ`)
  - `ActiveQuiz(quizId, type, question, options, durationSeconds, startTimestampMs, correctOptionIndex)`
  - `QuizSubmission(studentId, studentName, selectedOptionIndex, answerTimestampMs)`
- `WhiteboardUiState` additions:
  - `activeQuiz: ActiveQuiz?`
  - `userQuizAnswer: Int?`
  - `quizSubmissions: List<QuizSubmission>`
  - `quizTimeRemainingSeconds: Int`
- Host Quiz Launcher Deck:
  - Create poll or timed quiz with question text, options (up to 6), timer countdown (15s, 30s, 60s, none), and optional correct answer.
  - Broadcasts `QuizStart`.
  - Aggregates student responses live with real-time percentage distribution bars.
  - Ends quiz manually or automatically on timer expiration; broadcasts `QuizEnd` with correct answer and reveals leaderboard.
- Student Interactive Quiz Overlay:
  - Material 3 Expressive animated card overlay displaying the question, active countdown timer bar, and option buttons.
  - Tapping an option locks in the vote, triggers subtle haptic feedback, and sends `QuizAnswer`.
  - When quiz concludes, displays result feedback (correct/incorrect) and total class distribution.

## Verification
- Unit test in `QuizEngineTest.kt` verifying quiz broadcast, answer aggregation, percentage calculation, and leaderboard scoring.
- Unit test verifying timer expiration closes voting.
- `./gradlew :app:testDebugUnitTest` passes.
