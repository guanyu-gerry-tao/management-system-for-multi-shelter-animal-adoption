# CLAUDE.md — Multi-Shelter Animal Adoption Management System

## Project Overview

**Course**: CS 5004, Northeastern University
**Type**: Final Project (Java OOD)
**Deadline**: 2026-04-21 12:00 PM PT (final GitHub push)
**Presentation**: 2026-04-21 1:00–4:00 PM (in-class demo + Q&A)

The goal is a Java-based multi-shelter animal adoption management system that demonstrates object-oriented design principles, not a complex UI.

---

## Architecture (Three Layers)

```
Strategy Layer  ←  encapsulates pluggable rules (MatchingStrategy, VaccinationStrategy...)
Service Layer   ←  coordinates business workflows (AdoptionService, MatchingService...)
Domain Layer    ←  core entities (Animal, Shelter, Adopter, AdoptionRequest...)
```

**Domain Layer**:
- `Animal` (abstract base) → `Dog`, `Cat`, `Rabbit`
- `Shelter`: holds a collection of animals, manages capacity
- `Adopter`: adopter info, preferences, and lifestyle context
- `AdoptionRequest`: links adopter to animal, tracks request status
- `TransferRequest`: models inter-shelter animal transfers

**Service Layer**:
- `AdoptionService`: manages adoption request lifecycle
- `TransferService`: handles inter-shelter transfers
- `MatchingService`: scores and ranks animals using strategies
- `VaccinationService`: tracks vaccination records and reminders
- `RequestNotificationService`: dispatches status change notifications
- `ExplanationService` (interface) → `AIExplanationService` / `MockExplanationService`

**Strategy Layer**:
- `MatchingStrategy` (interface) → `BreedStrategy`, `ActivityStrategy`, `LifestyleStrategy`
- Vaccination strategies vary by animal type and schedule

---

## Scoring Rubric (100 pts)

| Category | Points |
|---|---|
| Submitted & Presented on Schedule | 23 |
| Content (complexity, creativity, runnable, CS5004 relevance, citations) | 27 |
| Code Quality (Javadoc, decomposition, data structures, test coverage) | 16 |
| OOD Design Principles (22 principles) | 22 |
| Presentation Quality (slides, timing, demo, explanation) | 12 |

---

## OOD Design Principles (must follow strictly)

1. All public classes and methods must have Javadoc (at least two sentences, beyond just restating the definition)
2. Prefer interface types over concrete types (except immutable value objects)
3. Fields must be `private` (except constants); methods and classes should be as private as possible
4. Classes must not have public methods beyond what the interface defines (constructors excepted)
5. Catch and handle/report errors as early as possible
6. Validate input at system boundaries
7. Use exceptions only for truly exceptional conditions, not for flow control
8. Be mindful of references, copies, and mutation; use defensive copies where necessary
9. Single responsibility: one class does one thing
10. Don't repeat yourself (DRY)
11. Open/closed principle: open for extension, closed for modification
12. Design for extensibility: easy to add new rules or types
13. Write tests first; cover edge cases; don't expose fields or add public methods just for testing
14. Prefer loose coupling; write reusable components
15. Once an interface is published, do not change it
16. Reuse existing exceptions, classes, libraries, and design patterns

---

## Code Standards

- **Language**: Java (must be predominantly Java)
- **Naming**: Java conventions — `camelCase` for methods/variables, `PascalCase` for classes
- **Comments**: Javadoc on all public classes and methods; inline comments for complex logic
- **Testing**: JUnit unit tests covering normal paths and edge cases; use `MockExplanationService` for AI module tests
- **Build**: Gradle (`build.gradle` in project root)

---

## AI Integration Notes

- `AIExplanationService` calls an external AI API as a post-processing step to generate natural-language match summaries
- AI does not affect matching scores — scoring logic lives entirely in the Strategy layer
- Use `MockExplanationService` in tests to ensure deterministic output
- API keys must never be committed to the repository

---

## File Structure

- [CONTRIBUTING.md](CONTRIBUTING.md) — team roles, branch strategy, commit conventions
- [docs/proposal.md](docs/proposal.md) — machine-readable proposal with Mermaid diagrams
- [docs/proposal-for-pdf.md](docs/proposal-for-pdf.md) — human-readable proposal
- [docs/diagram-class.mmd](docs/diagram-class.mmd) — class diagram (Mermaid source)
- [docs/diagram-layer.mmd](docs/diagram-layer.mmd) — layer architecture diagram
- [docs/Final_Project_Instructions.pdf](docs/Final_Project_Instructions.pdf) — original assignment instructions

---

## Development Priority

1. **Domain Layer first** — Animal hierarchy, Shelter, Adopter, Request classes
2. **Strategy Layer** — MatchingStrategy interface + 3 implementations
3. **Service Layer** — AdoptionService first, then MatchingService
4. **ExplanationService** — MockExplanationService first, real AI API last
5. **Tests in parallel** — write JUnit tests alongside each module
6. **Javadoc as you go** — do not leave it to the end

---

## Git & Collaboration

See [CONTRIBUTING.md](CONTRIBUTING.md) for branch strategy, commit message format, and team responsibilities.
