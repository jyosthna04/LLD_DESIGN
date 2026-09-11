# Research Note — DesignLoop

**1. Problem.** Learners preparing for software engineering interviews
(or simply trying to improve) need to practice Low-Level Design (LLD)
— designing classes, responsibilities, and relationships for a system.
Unlike algorithmic coding problems, LLD has no single "correct"
answer and no compiler to check it, so learners rarely get useful,
specific feedback on their designs.

**2. Target learner.** Early-career or student developers preparing
for technical interviews, or engineers who want a low-stakes space to
practice system/class design before doing it in a real interview or
real project.

**3. Existing ways learners practice LLD.** Mock interviews with
peers or mentors, reading solution write-ups/blog posts after
attempting a problem alone, or paid interview-prep platforms with
human reviewers.

**4. Problems with existing approaches.** Peers are not always
available, and peer feedback quality varies widely. Reading a "model
solution" without attempting your own design first teaches
recognition, not the actual skill of designing. Human-reviewed
platforms are slow (hours/days turnaround) and often costly.

**5. Why LLD evaluation is different from normal coding evaluation.**
Coding problems usually have a correctness oracle: run the code
against test cases, get a pass/fail. LLD has no such oracle — a
"ParkingLot" design with 3 classes and one with 6 classes can both be
reasonable, depending on trade-offs the designer made. Evaluation
has to combine hard facts (did you address the stated requirements?)
with judgement (is the design well-structured, following good
principles?).

**6. Why multiple LLD designs can be valid.** Different learners will
reasonably choose different abstractions, different numbers of
classes, and different trade-offs (e.g. simplicity vs. extensibility)
for the same problem. The evaluator should not penalize valid
alternative designs — it should check that requirements are covered
and that whatever structure was chosen follows sound design
principles, not compare against one fixed "correct" solution.

**7. Why hybrid deterministic + AI evaluation is useful.** Deterministic
checks are fast, free, 100% consistent, and perfect for objective
facts (missing classes, duplicates, empty submissions). But they
cannot judge design quality itself. An LLM can reason about
responsibility separation, coupling, and SOLID principles the way a
human reviewer would — but is slower, costs money per call, and can
occasionally fail or return malformed output, so it should never be
the sole mechanism the app depends on.

**8. MVP scope.** 3 problems (Parking Lot, Elevator System, Vending
Machine), a simple text-based design editor (not a visual UML tool),
synchronous hybrid evaluation, and attempt history. No user accounts,
no async job processing, no complex UML parsing.

**9. Assumptions.** Single implicit user (no login) for this MVP.
Learners will describe their design in reasonably plain English/class
notation rather than formal UML syntax. A Groq API key may or
may not be configured; the app must remain useful either way.
