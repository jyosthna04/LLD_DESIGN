# DesignLoop — LLD Practice & Feedback

> Practice LLD. Submit your design. Understand why it works — and how to make it better.

DesignLoop is a small web app for practicing Low-Level Design (LLD) — the
skill of designing classes, responsibilities, and relationships for a
software system (e.g. "design a Parking Lot"). Learners pick a problem,
write a design, submit it, and get structured feedback: a score, what
they did well, what's wrong (and *why*), and concrete suggestions.

## Features

- 3 starter LLD problems: Parking Lot, Elevator System, Vending Machine
- Simple design editor: classes, responsibilities, interfaces, relationships, explanation, trade-offs
- **Hybrid evaluation**: fast, reliable code-based checks + an LLM for subjective design judgement
- Feedback page with score, strengths, problems (with reasoning), suggestions, missing requirements, trade-offs
- History of past attempts per problem, with the ability to view old feedback
- Retry evaluation if the AI call fails — your submission is never lost
- Graceful handling of empty submissions, duplicate classes, missing concepts, and AI/API failures

## Technology Stack

- **Backend:** Java 17, Spring Boot 3, Maven, Spring Web, Spring Data JPA, Hibernate
- **Database:** MySQL (H2 in-memory is used automatically for tests only)
- **Frontend:** Thymeleaf, HTML, CSS (no JS framework — kept intentionally simple)
- **AI:** Groq API (GPT-OSS 120B) for subjective design feedback
- **Testing:** JUnit 5, Spring Boot Test

## Architecture

```
Browser
  ↓
Controller     (receives HTTP requests, picks the HTML page to render)
  ↓
Service        (AttemptService — business logic: start/save/submit/retry)
  ↓
Evaluator      (DeterministicEvaluator + LlmEvaluator → FeedbackMerger)
  ↓
Repository     (Spring Data JPA — talks to MySQL)
  ↓
Entity/Model   (Problem, Attempt, Submission, Evaluation)
```

### Evaluation design (the core of this project)

```
Submission
   ↓
DeterministicEvaluator   +   LlmEvaluator
   ↓
     FeedbackMerger
   ↓
EvaluationResult → Evaluation (saved to DB)
```

Both evaluators implement the same `Evaluator` interface:

```java
public interface Evaluator {
    EvaluationResult evaluate(Problem problem, Submission submission);
}
```

This means a **new evaluator can be added later without touching any
existing code** — just implement `Evaluator` and plug it into
`AttemptService`. For example, a future `StyleEvaluator` that checks
naming conventions.

- **DeterministicEvaluator** — plain Java. Checks: is the submission
  empty? Are all required concepts/classes present? Are there
  duplicate class names? Did the learner describe relationships when
  they have multiple classes? These are objective facts, so we don't
  waste an AI call on them.
- **LlmEvaluator** — calls the Groq API and asks it to judge
  subjective qualities (separation of responsibilities, coupling,
  cohesion, SOLID, extensibility). The AI is forced to reply in
  **strict JSON**, which is parsed into a Java DTO
  (`LlmFeedbackDto`) — this means arbitrary/broken AI text can never
  crash the app; on any parsing or network failure we fall back to
  deterministic-only feedback instead.
- **FeedbackMerger** — combines both results into one `Evaluation`
  (40% deterministic, 60% AI, or 100% deterministic if AI was
  unavailable).

## Database Setup

You need MySQL running locally. You do **not** need to manually create
the database — Spring Boot creates it automatically on first run
(`createDatabaseIfNotExist=true` in `application.properties`).

**Using MySQL Workbench (optional, if you want to look at the data):**
1. Open MySQL Workbench and connect to your local MySQL server.
2. Once the app has run once, refresh the schema list on the left —
   you'll see a `designloop` schema with tables `problems`,
   `attempts`, `submissions`, `evaluations`.
3. You can right-click any table → "Select Rows" to view its data.

If you'd rather create the schema by hand, this is the SQL Spring Boot
effectively generates for you (you don't need to run this yourself):

```sql
CREATE DATABASE IF NOT EXISTS designloop;
```

Hibernate then creates the tables automatically based on the `@Entity`
classes (`spring.jpa.hibernate.ddl-auto=update`).

**Update your credentials** in `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=root
```
Change `root`/`root` to match your own local MySQL username/password.

## How to Configure the AI API Key

The app needs a Groq API key for the LLM part of the evaluation.
Get a free key at https://console.groq.com/keys, then set it as an
environment variable before running the app:

```bash
# Mac/Linux
export GROQ_API_KEY=gsk_xxxxxxxxxxxxx

# Windows (cmd)
set GROQ_API_KEY=gsk_xxxxxxxxxxxxx
```

**If you don't set this, the app still works** — it just skips the AI
part and shows deterministic (code-based) feedback only, with a note
on the Feedback page saying AI was unavailable.

## How to Run the Application

```bash
cd designloop
mvn spring-boot:run
```

Then open: **http://localhost:8080**

On first run, the app automatically inserts the 3 starter problems
into the database (see `DataSeeder.java`).

## How to Run Tests

```bash
mvn test
```

Tests use an in-memory H2 database automatically (see
`src/test/resources/application.properties`), so you don't need MySQL
running to run the test suite.

## Example Usage

1. Go to http://localhost:8080 — you'll see the 3 problems.
2. Click "View Details" on **Parking Lot**, read the requirements, click **Start Practice**.
3. Fill in classes like:
   ```
   ParkingLot: manages slots and issues tickets
   Vehicle: represents a parked vehicle
   Slot: a single parking space
   Ticket: records entry time and vehicle info
   ```
   Fill in a relationship like `ParkingLot has many Slots. Slot holds one Vehicle.`
4. Click **Submit for Feedback**.
5. See your score, strengths, problems (with explanations), and suggestions.
6. Go back and click **History** to see all your attempts for this problem.

## Known Limitations

- Single implicit user — there is no login/authentication. All attempts are visible to anyone using the app.
- Evaluation runs synchronously (the learner waits a few seconds during the AI call) — fine for an MVP, not for high traffic.
- The design editor uses plain text fields rather than a visual UML tool, by design (kept simple for a 2-day scope).
- Deterministic checks are keyword-based (they check whether a required concept's name appears in the text), not a real code/UML parser.

## Future Improvements

- User accounts and login
- Async evaluation with a status-polling UI (no waiting on the request)
- Structured (not free-text) class/relationship editor with drag-and-drop
- More problems, with configurable difficulty and topic tags
- Compare two attempts side by side to show improvement over time
- Add more `Evaluator` implementations (e.g. naming-convention checks) — trivial thanks to the `Evaluator` interface
