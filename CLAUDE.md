# CLAUDE.md — Multi-Shelter Animal Adoption Management System

## Project Overview

**Course**: CS 5004, Northeastern University
**Type**: Final Project (Java OOD)
**Deadline**: 2026-04-21 12:00 PM PT (final GitHub push)
**Presentation**: 2026-04-21 1:00–4:00 PM (in-class demo + Q&A)

The goal is a Java-based multi-shelter animal adoption management system that demonstrates object-oriented design principles. The system is operated via a CLI tool called `shelter` and is designed to be demoed using an AI agent (Claude Code) that interprets natural language and executes CLI commands.

---

## Architecture (Five Layers)

```
Presentation Layer  ←  CLI entry points (shelter animal admit ..., shelter adopt approve ...)
Application Layer   ←  orchestrates use cases across multiple services
Service Layer       ←  single-responsibility business operations
Strategy Layer      ←  pluggable scoring rules (MatchingStrategy...)
Domain Layer        ←  core entities (Animal, Shelter, Adopter, AdoptionRequest...)
```

**Domain Layer** (`shelter.domain`):
- `Animal` (abstract base) → `Dog`, `Cat`, `Rabbit`
  - `adopterId`: `null` = available; non-null = adopted by that adopter
- `Shelter`: holds a collection of animals, manages capacity
- `Adopter`: adopter info, preferences, lifestyle context
  - `adoptedAnimalIds`: list of all animal IDs this adopter has adopted
- `AdoptionRequest`: links adopter to animal, tracks request status
- `TransferRequest`: models inter-shelter animal transfers
- `Staff`: the operator performing actions (single hardcoded admin in demo)
- `VaccineType`: a type of vaccine with name, applicable species, validity period, and ID
- `VaccinationRecord`: a single vaccination event for an animal

**Service Layer** (`shelter.service`):
- `AdoptionService`: manages adoption request lifecycle (submit/approve/reject/cancel)
- `TransferService`: handles inter-shelter transfers (request/approve/reject/cancel)
- `AdopterBasedMatchingService`: ranks animals for a given adopter
- `AnimalBasedMatchingService`: ranks adopters for a given animal
- `VaccinationService`: records vaccinations, checks overdue, retrieves history
- `VaccineTypeCatalogService`: CRUD for vaccine types
- `RequestNotificationService`: dispatches status change notifications
- `ExplanationService` (interface) → `AIExplanationService` / `MockExplanationService`
- `AnimalService`, `AdopterService`, `ShelterService`, `StaffService`, `AuditService`

**Application Layer** (`shelter.application`):
- Orchestrates multiple service calls to fulfill one complete use case
- Each method corresponds to one use case in `docs/use-cases.md`
- The CLI layer calls application layer methods, not service methods directly
- Classes: `AnimalApplicationService`, `AdopterApplicationService`, `ShelterApplicationService`, `AdoptionApplicationService`, `TransferApplicationService`, `MatchingApplicationService`, `VaccinationApplicationService`, `AuditApplicationService`

**Strategy Layer** (`shelter.strategy`):
- `IMatchingStrategy` (interface) → `SpeciesPreferenceStrategy`, `BreedPreferenceStrategy`, `ActivityLevelStrategy`, `AgePreferenceStrategy`, `LifestyleCompatibilityStrategy`, `VaccinationPreferenceStrategy`
- `MatchingCriterion` (enum), `MatchingPreferencesProfile`, `MatchingPreferencesPriority`

**Presentation Layer** (`shelter.cli`):
- Picocli-based CLI tool registered as `shelter` command
- Each subcommand reads `--` arguments, merges with existing data if needed, then calls the application layer
- Does not contain business logic

---

## CLI Usage

The system is operated as a stateless CLI. Data is persisted to `~/shelter/data/` as CSV files. Each command loads data, performs the operation, and saves back.

```
shelter shelter list
shelter shelter register --name "Happy Paws" --location "Boston" --capacity 20
shelter animal list [--shelter <id>]
shelter animal admit --species dog --name "Max" --breed "Labrador" --age 3 --activity MEDIUM --shelter <id>
shelter adopt submit --adopter <id> --animal <id>
shelter adopt approve --request <id>
shelter match animal --adopter <id> --shelter <id> [--explain]
shelter match adopter --animal <id> [--explain]
shelter vaccine record --animal <id> --type <name> --date <yyyy-mm-dd>
shelter vaccine overdue --animal <id>
```

For demo purposes, Claude Code is used as the AI agent: the user speaks natural language, Claude interprets the intent and executes the appropriate `shelter` commands.

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
- **Build**: Gradle with `application` plugin; main class is `shelter.cli.Main`; CLI dependency is Picocli

### Domain Class Requirements

Every domain class must implement the following:

- **Javadoc**: all classes, fields, constructors, and methods must have Javadoc (minimum two sentences)
- **Getters**: all fields must have a getter
- **Setters**: all non-`final` fields must have a setter with input validation
- **toString**: all classes must override `toString()` with meaningful output
- **hashCode and equals**: based on the business identifier (`id` for entity classes; all fields for value objects)
- **compareTo**: classes with natural ordering must implement `Comparable`; ordering rules:
  - Named entities (`Animal`, `Shelter`, `Adopter`, `Staff`, `VaccineType`) → by `name` alphabetically
  - Request classes (`AdoptionRequest`, `TransferRequest`) → by submission timestamp ascending
  - `VaccinationRecord` → by `dateAdministered` ascending
  - Classes with no natural ordering (`AdopterPreferences`, enums) → do not implement `Comparable`
- **Constructors**: every class must explicitly define:
  - **Full constructor**: all fields as parameters (primary constructor used in code)
  - **Copy constructor**: takes an instance of the same class and copies all fields
  - **No-arg constructor**: only for classes with no `final` fields; fields default to `null` / `0`; note that most entity classes have a `final String id` and therefore cannot have a no-arg constructor

---

## AI Integration Notes

- `AIExplanationService` calls an external AI API as a post-processing step to generate natural-language match summaries
- AI does not affect matching scores — scoring logic lives entirely in the Strategy layer
- Use `MockExplanationService` in tests to ensure deterministic output
- API keys must never be committed to the repository
- For demo: Claude Code acts as the AI agent, interpreting natural language and calling `shelter` CLI commands

---

## File Structure

- [CONTRIBUTING.md](CONTRIBUTING.md) — team roles, branch strategy, commit conventions
- [docs/use-cases.md](docs/use-cases.md) — full use case list with application layer method signatures
- [docs/proposal/proposal-for-pdf.md](docs/proposal/proposal-for-pdf.md) — human-readable proposal
- [docs/diagram-class.mmd](docs/diagram-class.mmd) — class diagram (Mermaid source)
- [docs/diagram-layer.mmd](docs/diagram-layer.mmd) — layer architecture diagram
- [docs/Final_Project_Instructions.pdf](docs/Final_Project_Instructions.pdf) — original assignment instructions

---

## Git & Collaboration

See [CONTRIBUTING.md](CONTRIBUTING.md) for branch strategy, commit message format, and team responsibilities.
