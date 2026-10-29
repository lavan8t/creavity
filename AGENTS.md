# CreaVITy

CreaVITy is a modern, high-performance offline collaborative digital whiteboard for Android, built with Jetpack Compose, Material 3 Expressive, and Google Jetpack Ink. See `README.md` for the full feature list and tech stack.

## Agent skills

### Navigation

Canvas input pipeline (events → gestures → actions → renderer), collaboration pipeline (packets → routing → transport → handler → canvas caches), state lifetimes, sync debugging, and code search paths: `docs/agents/architecture.md`.

### Issue tracker

Issues and specs live as local markdown files in `.scratch/` (`.scratch/live-stroke/` for the current live-stroke bug). See `docs/agents/issue-tracker.md`.

### Triage labels

Default five-role vocabulary: `needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`. See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: `GLOSSARY.md` and `docs/adr/` at the repo root. See `docs/agents/domain.md`.
