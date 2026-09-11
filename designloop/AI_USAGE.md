# AI_USAGE.md

Honest notes on where AI was used while building this project, and the
reasoning behind each decision.

### 1. Using AI for all evaluation vs. a hybrid approach
**AI suggested:** using an LLM for the entire evaluation (score,
requirements coverage, everything).
**Decision:** Rejected for the objective parts. Whether a required
class is present, whether the submission is empty, or whether a class
name is duplicated are *facts*, not opinions — plain Java code checks
these instantly, for free, and 100% consistently. The AI is only used
for the genuinely subjective parts (SOLID, coupling, cohesion,
extensibility). This is why the app has a `DeterministicEvaluator`
*and* an `LlmEvaluator`, combined by a `FeedbackMerger`.

### 2. Free-text design editor vs. a visual UML builder
**AI suggested:** a drag-and-drop UML diagram editor for building
classes and relationships visually.
**Decision:** Rejected for this scope. A visual UML tool is a
multi-week project on its own and would compete for time with the
evaluation logic, which is the actual point of this assignment. A
small set of labeled text areas (classes, interfaces, relationships,
explanation, trade-offs) captures the same information with a
fraction of the engineering effort, and is realistic for a 2-day/1-day
build.

### 3. Forcing the LLM to answer in strict JSON
**AI suggested this, and it was accepted.** Free-form AI text is
impossible to reliably render on a feedback page or store in
structured database columns. Asking the model to reply in a fixed JSON
shape, and parsing that into a Java DTO (`LlmFeedbackDto`), means a
broken or unexpected AI response can be caught and handled gracefully
(falling back to deterministic-only feedback) instead of corrupting
the page or crashing the request.

### 4. Synchronous vs. asynchronous evaluation
**AI suggested** building a background job queue so evaluation
happens asynchronously and the learner doesn't wait.
**Decision:** Rejected for the MVP. A queue system adds real
complexity (a worker process, job status tracking, polling or
websockets on the frontend) for a problem that, at this scale, is
solved fine by just waiting a few seconds for a synchronous HTTP
response. This matches the assignment's explicit instruction to avoid
overengineering. Noted as a "Future Improvement" in the README instead.

### 5. Weighting between deterministic and AI scores
**AI suggested** a 40% deterministic / 60% AI split for the final
score.
**Decision:** Accepted, with reasoning: the deterministic checks
mostly catch missing/incomplete work, which is important but binary
(either you included the required class or you didn't). Most of what
separates a mediocre design from a great one is judgement — separation
of concerns, cohesion, extensibility — which is what the AI evaluates.
Weighting AI slightly higher reflects that, while deterministic checks
still meaningfully pull the score down when basics are missing.
