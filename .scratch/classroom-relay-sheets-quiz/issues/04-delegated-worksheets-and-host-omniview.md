# Ticket 04: Delegated Worksheets & Host Omniview Matrix

Status: resolved
Completed At: 2026-10-28T12:12:40+05:30
Blocked by: 01

## What it delivers
- Data models for private worksheets:
  - `StudentWorksheet(sheetId, studentId, studentName, title, questionText, strokes, isSubmitted, submittedAtMs)`
- `WhiteboardUiState` additions:
  - `activeWorksheet: StudentWorksheet?` (on student side)
  - `studentWorksheets: Map<String, StudentWorksheet>` (on host side)
- Host Delegation Action:
  - Host triggers `DelegateWorksheet(title, questionText, targetStudentId = null /* all */)`.
  - Dispatches `WhiteboardPacket.DelegateSheet`.
- Student Worksheet Canvas:
  - When a student receives `DelegateSheet`, they can switch to their dedicated worksheet.
  - Question prompt is pinned in an elegant top banner.
  - Strokes on the worksheet are encapsulated in `StudentSheetStroke` and routed solely to the host.
  - "Submit Worksheet" button transitions sheet to locked read-only mode and emits `SheetSubmission`.
- Host Omniview Matrix:
  - Host UI renders an "Omniview" grid showing live thumbnail previews of every student's active sheet.
  - Status indicators show whether the student is currently drawing, idle, or submitted.
  - Host can click any student tile to inspect their full-screen sheet and add teacher notes/feedback.

## Verification
- Unit test in `WorksheetSyncTest.kt` verifying student sheet strokes are only applied to the target worksheet and only received by the host.
- Unit test verifying worksheet submission locks student editing.
- `./gradlew :app:testDebugUnitTest` passes.
