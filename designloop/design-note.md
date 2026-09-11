# Design Note — DesignLoop

## Product flow
Choose Problem → Read Requirements → Start Attempt → Create/enter LLD
Design → Submit → Get Feedback → Review → Try Again.

## Architecture (layers)
- **Controller** — receives the HTTP request from the browser, calls
  the service layer, chooses which Thymeleaf template to render.
  (`ProblemController`, `AttemptController`)
- **Service** — business logic: starting attempts, saving drafts,
  running evaluation, retrying failed evaluations. (`AttemptService`)
- **Evaluator** — evaluates a submission. Split into
  `DeterministicEvaluator` (code checks) and `LlmEvaluator` (AI
  judgement), combined by `FeedbackMerger`.
- **Repository** — Spring Data JPA interfaces that talk to MySQL.
- **Entity/Model** — `Problem`, `Attempt`, `Submission`, `Evaluation`
  represent the application's data.

## Domain model
```
Problem (1) ──< Attempt (many)
Attempt (1) ──1 Submission
Attempt (1) ──1 Evaluation
```
- **Problem** — a practice question with requirements and the list of
  concepts a valid design should include.
- **Attempt** — one try at a problem. Has a status
  (STARTED → SUBMITTED → EVALUATING → COMPLETED, or → FAILED) and an
  attempt number (1st try, 2nd try, ...).
- **Submission** — the actual design text the learner entered
  (classes, interfaces, relationships, explanation, trade-offs).
- **Evaluation** — the result: overall score, strengths, problems,
  suggestions, missing requirements, trade-off notes, and whether AI
  was available when this was generated.

## Why each class exists / why responsibilities are separated
Each entity maps to one clear concept so that no single class is
responsible for more than one job — the same principle the app itself
is teaching. `Attempt` tracks *progress through the flow* (status);
`Submission` holds *what the learner wrote*; `Evaluation` holds *what
we concluded about it*. Keeping them separate means a submission is
never lost even if evaluation fails, and a learner can retry
evaluation without retyping anything.

## Database design
Tables: `problems`, `attempts` (FK → problem), `submissions` (FK →
attempt), `evaluations` (FK → attempt). Primary keys are
auto-generated IDs. `attempts.status` is a string enum column.
Timestamps (`created_at`, `submitted_at`) are stored for history/audit.
Hibernate creates/updates these tables automatically from the
`@Entity` classes (`ddl-auto=update`), so no manual SQL is required for
day-to-day development.

## Evaluation design
```
Submission
   ↓
DeterministicEvaluator  +  LlmEvaluator
   ↓
     FeedbackMerger
   ↓
Evaluation Result (saved)
```
Both evaluators implement a shared `Evaluator` interface so the
service layer doesn't need to know how either one works internally —
it just calls `evaluate(problem, submission)` and gets back the same
`EvaluationResult` shape either way. This is what makes the system
**extensible**: a new evaluator (e.g. a naming-convention checker) can
be added later just by implementing `Evaluator`, with zero changes to
existing code.

`DeterministicEvaluator` runs first and checks objective facts.
`LlmEvaluator` calls the Groq API with a prompt that forces a
strict JSON reply, parsed into a `LlmFeedbackDto` — this boundary is
what stops "arbitrary AI output" from ever reaching the database or
the UI unvalidated. `FeedbackMerger` combines both into one
`Evaluation` (40% deterministic / 60% AI weighting, or 100%
deterministic if the AI call failed).

## Error handling
- Empty submission / no classes → deterministic check catches it, score 0, clear message.
- Missing required concept / duplicate class → deterministic check flags it by name.
- AI API failure or malformed AI response → caught inside `LlmEvaluator`; it reports itself as "unavailable" instead of throwing, and the merger falls back to deterministic-only scoring.
- Any other unexpected error during evaluation → caught in `AttemptService.runEvaluation`, attempt is marked `FAILED`, the submission is preserved, and the learner can hit "Retry Evaluation" on the Feedback page.
- Resubmitting an already-completed attempt → rejected with a clear `IllegalStateException`, shown as a friendly message via `GlobalExceptionHandler`.
- Unknown problem/attempt IDs → `NoSuchElementException`, also shown as a friendly error page instead of a stack trace.

## Extensibility
- New evaluators: implement `Evaluator`, no other changes needed.
- New problems: insert a new `Problem` row (or extend `DataSeeder`) with its required concepts.
- New evaluation criteria for the AI: extend the prompt and `LlmFeedbackDto`.

## Trade-offs
- Free-text design fields instead of a structured/visual UML builder — much simpler to build and use, at the cost of not preventing typos or enforcing formal syntax.
- Synchronous evaluation instead of an async job queue — simpler, at the cost of the learner waiting a few seconds during the AI call.
- Keyword-based deterministic checks instead of real parsing — simple and fast, at the cost of being technically foolable (e.g. mentioning a word without it being a real class).
