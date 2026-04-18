# Deck Content — Multi-Shelter Animal Adoption Management System

One section per slide. Edit freely; tell me what changed and I'll sync into `deck.html`.
Slide boundaries are marked by `---`.

---

## Presentation rules (from course instructor — 2026-04-17)

### Logistics — "Project Presentations"

- **Date:** April 21st, 2026
- **Time:** 1:00 – 4:20 PM
- Groups need to sign up for a time slot using the link which will be posted to Canvas.
  - First signup, first served.
- All groups must log into the Zoom link **BEFORE 1:00 PM**.
  - Link is the link to our lectures on Canvas.
  - Groups needing to be admitted after 1:00 PM will lose time from their allocated time slot.
- You will be **stopped after 8 minutes** and therefore will lose points for items not presented.
- All group members should participate equally in the presentation to get individual grades.
- Possibility of individual one-to-one meetings to further dig deep into understanding of the project.

### Structure — "Presentation"

- **Duration: 10 minutes** (8-minute demo, 2-minute Q&A).
- **Introduction (1–2 minutes)**
  - Summarize the project: objectives, design, and application goals.
- **Demonstration (5–6 minutes)**
  - Demonstration of your project in operation.
  - Methods and tools used.
  - Walk-through of a small portion of your code.
  - Findings and lessons learned (what was hard/easy?).
  - Highlights of your use of 1–2 big ideas from Object Oriented Design.
    - e.g., encapsulation, inheritance, composition, polymorphism, abstraction, MVC.
- **Conclusion (1–2 minutes)**
  - Explain your learnings of the course and how you applied them to this project.
  - Highlight what you learned from the project.
  - Summarize outcomes, potential improvements, and challenges you faced.
- **Scholarly citations.**

### Implications for this deck

- Slides are a companion to the live CLI demo, not the main content — target **~8 slides**.
- Each group member should own at least one section verbally.
- Live demo is the anchor; slides exist to frame intro + conclusion and highlight OOD ideas.

---

## Target time budget (9 slides, 10 min total)

| # | Slide | Rubric section | Owner | Time |
|---|---|---|---|---|
| 1 | Title | — | all | 10 s |
| 2 | Introduction — problem, objectives, goals | Intro | Yiying | 45 s |
| 3 | Architecture + methods & tools used | Intro / Methods | Gerry | 35 s |
| 4 | Why CLI, not GUI | Intro / Design | Yiying | 25 s |
| 5 | OOD big idea #1 — Strategy pattern (with code) | Demo / OOD | Yuxi | 65 s |
| 6 | OOD big idea #2 — Shared request lifecycle (with code) | Demo / OOD | Gerry | 35 s |
| 7 | LIVE DEMO (terminal) | Demo | Gerry drives, team narrates | 180 s |
| 8 | Takeaways — learned, next | Conclusion | Gerry | 45 s |
| 9 | AI use acknowledgment | Closing | all | 30 s |
| — | Q&A | — | all | 120 s |

Total slide time: 470 s (7:50) · Q&A: 120 s (2:00). Inside the 8-min + 2-min hard stop with 10 s cushion.

---

## Slide 1 — Title

**Layout:** title

**Kicker:** CS 5004 · Northeastern University · Spring 2026

**Title:** Multi-Shelter Animal Adoption Management System

**Subtitle:** An OOD-first Java application, driven by a natural-language CLI

**Date:** April 21, 2026

**Team:**
- **Guanyu Tao** — Service Layer + AI Integration
- **Yiying Xie** — Domain Layer
- **Yuxi Ou** — Strategy Layer + Matching

**Speaker notes:** 10 seconds. One sentence framing — who we are and what we built.

---

## Slide 2 — Introduction: problem, objectives, goals

**Layout:** bullets

**Kicker:** Introduction

**Title:** What we set out to build and why

**Problem:**
- Shelters track animals, adopters, and vaccines in separate spreadsheets.
- Cross-shelter matching is manual; vaccine deadlines get missed.
- No AI automation in this space, and Java isn't built for frontend UIs.

**Objectives:**
- Rigorous OOD across clean layers.
- One CLI — usable by humans *and* AI agents.
- Extensible: new species, matching rule, or request type = one new class.

**What it does:**
- Intake animals and adopters.
- Score adopter–animal compatibility.
- Run adoption + transfer workflows.
- Track vaccines + full audit log.

> 3 tabs verticlelly: Problem, Objectives, What it does.

**Speaker notes:** 60 s. Problem → three design pillars → capabilities. Don't read the slide; expand verbally.

---

## Slide 3 — Architecture, methods & tools

**Layout:** layer-stack + tools sidebar

**Kicker:** Design & methods

**Title:** Six-layer architecture, one direction of dependency

**Layers (top → bottom):**
- **Presentation** — Picocli commands: parse `--` args → call Application → print. No logic.
- **Application** — One method per use case (`admitAnimal`, `approveAdoption`, …): load via Repository, call Services, save.
- **Service** — Business logic per concern: `AdoptionService`, `TransferService`, `MatchingService`, `VaccinationService`, `AuditService`.
- **Strategy** — Six `IMatchingStrategy` impls; `MatchingScoreCalculator` composes them.
- **Repository** — Interfaces + CSV impls (`AnimalRepository` → `CsvAnimalRepository`, …): the only code that touches disk.
- **Domain** — Entity classes (`Animal`, `Shelter`, `Adopter`, …): validated constructors, no I/O.
> the 5 layers part should be the first thing, and the main focus of this page. 

**Tools:** Java 17 · Gradle · Picocli · JUnit 5 · CSV (no DB).

**Speaker notes:** 45 s. One sentence per layer, then tools as a flyover.

---

## Slide 4 — Why CLI, not GUI

**Layout:** title + one-liner + bullets

**Kicker:** Design choice

**Title:** Why CLI, not GUI

**One-liner:** Java is built for backend logic. The CLI turns that into a universal API surface.

**Bullets:**
- **Right tool for the job.** Java earns its keep on logic, not rendering pixels.
- **One surface, many callers** — humans in the terminal, AI agents (Claude Code), and 78 integration tests hit the same commands.
- **Future-proof.** A JS/web UI can layer on top by calling the CLI — Java core stays untouched.

**Speaker notes:** 30 s. Lead with "Java is backend-first," then the three-callers point with AI agents emphasized, then wave at the extensibility path. Don't dwell — this frames the code walkthroughs that follow.

---

## Slide 5 — OOD big idea #1: Strategy pattern

**Layout:** title + one-liner + code

**Title:** Strategy pattern

**One-liner:** One interface, six matching rules, zero `if`-ladders.

**Code:**
```java
public interface IMatchingStrategy {
    MatchingCriterion getCriterion();
    boolean isApplicable(Adopter adopter, Animal animal);
    double score(Adopter adopter, Animal animal);
}

public class SpeciesPreferenceStrategy    implements IMatchingStrategy { ... }
public class BreedPreferenceStrategy      implements IMatchingStrategy { ... }
public class AgePreferenceStrategy        implements IMatchingStrategy { ... }
public class ActivityLevelStrategy        implements IMatchingStrategy { ... }
public class LifestyleCompatibilityStrategy implements IMatchingStrategy { ... }
public class VaccinationPreferenceStrategy  implements IMatchingStrategy { ... }
```

**Speaker notes:** 75 s. Walk the 3 methods (criterion, applicable, score), then point at the 6 implementers. Close with "a seventh rule is one more class — nothing else changes."

---

## Slide 6 — OOD big idea #2: Shared request lifecycle

**Layout:** title + one-liner + code

**Title:** Shared request lifecycle

**One-liner:** Two request types, one state machine.

**Code:**
```java
public enum RequestStatus { PENDING, APPROVED, REJECTED, CANCELLED }

public class AdoptionRequest {
    public void approve();
    public void reject();
    public void cancel();
}

public class TransferRequest {
    public void approve();
    public void reject();
    public void cancel();
}
```

**Speaker notes:** 45 s. Point at the enum → both classes carry the same three verbs → "adding a third request type reuses all of it." Mention `RequestNotificationService` verbally.

---

## Slide 7 — LIVE DEMO

**Layout:** full-bleed divider

**Kicker:** Demonstration

**Title:** Live: Claude Code operating the `shelter` CLI

**Demo scaffold:**
- Register 2 shelters.
- Admit a dog, a cat, a fish (shows `Other` extensibility).
- Register an adopter → `shelter match animal`.
- Submit + approve adoption.
- Transfer across shelters.
- Record vaccine + overdue check + audit log.

**Speaker notes:** ~3.5 min. Gerry drives; Yuxi narrates strategy firing; Yiying narrates domain invariants. Fall back to typed commands if AI stalls. Full script: `docs/demo-plan.md`.

---

## Slide 8 — Takeaways

**Layout:** three short sections

**Kicker:** Conclusion

**Title:** Takeaways

**Learned:**
- Clear structure → problems are easy to locate and code is easy to reuse.
- Layers don't duplicate work — the separation paid off.
- 3-person coordination was the real challenge — locking public interfaces early saved us.

**Next:**
- CSV → real DB · add a REST layer · role-based access.
- Add a JavaScript/TypeScript dashboard and a dedicated AI system.

**Speaker notes:** 50 s. Learned → next. Close with "happy to go deeper in Q&A."

---

## Slide 9 — AI use acknowledgment

**Layout:** three short sections

**Kicker:** Transparency note

**Title:** How AI fit into this project

**Tools:**
- Claude Code + Codex as pair-programming partners.
- `CLAUDE.md` and `AGENTS.md` in the repo — shared context so humans and agents stay in sync.

**Process:**
- Research → planning → implementation with human in the loop.
- Test-driven: unit tests, integration tests, human acceptance runs.
- Docs-first: use cases, demo plan, integration-test plan.
- This deck: built with reveal.js 5.1, highlight.js, a custom auto-fit script, and Claude Code; reviewed via Chrome MCP screenshots.

**Speaker notes:** 25 s. Open by noting the professor's position that AI use is acceptable. Walk tools → process, close on the deck-tooling line so the audience understands the footprint is fully disclosed. Convey discipline, not novelty.

---

## Q&A (not a slide — 2 minutes)

Likely questions to pre-think:
- Why CSV and not a database? → scope; domain model is the interesting part.
- How do you add a new species? → subclass `Animal`, handle in CSV repo, done — no enum edits.
- What stops AI from changing match scores? → scoring is a separate service; AI only reads its output.
- Is the test suite deterministic? → yes, thanks to `MockExplanationService` and `SHELTER_HOME` isolation.
