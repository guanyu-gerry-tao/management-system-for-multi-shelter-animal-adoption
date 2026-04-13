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
- `Animal` (abstract base) → `Dog`, `Cat`, `Rabbit`, `Other` (free-form species name)
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

### Shelter (UC-01)
```
shelter shelter list
shelter shelter register --name <name> --location <location> --capacity <n>
shelter shelter update --id <id> [--name <name>] [--location <location>] [--capacity <n>]
shelter shelter remove --id <id>
```

### Animal (UC-02)
```
shelter animal list [--shelter <id>]
# list columns: ID / Species / Name / Breed / Age / Activity / Neutered / Indoor / Size / Fur / Status
# species-specific columns show "N/A" when not applicable to that species

shelter animal admit --species dog    --name <name> --breed <breed> --age <years> --activity <LOW|MEDIUM|HIGH> --shelter <id> [--size <SMALL|MEDIUM|LARGE>] [--neutered]
shelter animal admit --species cat    --name <name> --breed <breed> --age <years> --activity <LOW|MEDIUM|HIGH> --shelter <id> [--indoor] [--neutered]
shelter animal admit --species rabbit --name <name> --breed <breed> --age <years> --activity <LOW|MEDIUM|HIGH> --shelter <id> [--fur <SHORT|LONG>]
shelter animal admit --species other  --name <name> --breed <breed> --age <years> --activity <LOW|MEDIUM|HIGH> --shelter <id> --species-name <e.g. fish>
# --neutered in admit is a boolean flag (presence = true); in update it takes a value: --neutered true|false

shelter animal update --id <id> [--name <name>] [--activity <LOW|MEDIUM|HIGH>] [--neutered <true|false>]
# --neutered applies to dogs and cats only; silently ignored for rabbit/other
shelter animal remove --id <id>
```

### Adopter (UC-03)
```
shelter adopter list
# list columns: ID / Name / Living Space / Schedule / Species / Breed / Activity / Vaccinated / Min Age / Max Age
# unset preference fields show "any"

shelter adopter register --name <name> --space <APARTMENT|HOUSE_NO_YARD|HOUSE_WITH_YARD> --schedule <HOME_MOST_OF_DAY|AWAY_PART_OF_DAY|AWAY_MOST_OF_DAY> [--species <DOG|CAT|RABBIT|OTHER>] [--breed <breed>] [--activity <LOW|MEDIUM|HIGH>] [--requires-vaccinated <true|false>] [--min-age <n>] [--max-age <n>]
shelter adopter update --id <id> [--name <name>] [--space <...>] [--schedule <...>] [--species <...>] [--breed <...>] [--activity <...>] [--requires-vaccinated <true|false>] [--min-age <n>] [--max-age <n>]
shelter adopter remove --id <id>
```

### Matching (UC-04)
```
shelter match animal  --adopter <id> --shelter <id>
shelter match adopter --animal <id>
```

### Adoption (UC-05)
```
shelter adopt submit  --adopter <id> --animal <id>
shelter adopt approve --request <id>
shelter adopt reject  --request <id>
shelter adopt cancel  --request <id>
```

### Transfer (UC-06)
```
shelter transfer request --animal <id> --from <shelter-id> --to <shelter-id>
shelter transfer approve --request <id>
shelter transfer reject  --request <id>
shelter transfer cancel  --request <id>
```

### Vaccination (UC-07)
```
shelter vaccine record  --animal <id> --type <vaccine-type-name> --date <yyyy-mm-dd>
shelter vaccine overdue --animal <id>
shelter vaccine type list
shelter vaccine type add    --name <name> --species <DOG|CAT|RABBIT|OTHER> --days <n>
shelter vaccine type update --id <id> [--name <name>] [--species <...>] [--days <n>]
shelter vaccine type remove --id <id>
```

### Audit (UC-08)
```
shelter audit log
```

### Demo workflow note
IDs are not known in advance — always run `list` first to retrieve them. Typical sequence:
1. `shelter shelter list` → get shelter ID
2. `shelter animal list` / `shelter adopter list` → get animal/adopter IDs
3. Run the target command with the retrieved IDs

For demo purposes, Claude Code is used as the AI agent: the user speaks natural language, Claude interprets the intent and executes the appropriate `shelter` commands.

### Agent behavior rules (enforced during demo)

**Deletion requires confirmation.** Before executing any `remove` command (`shelter shelter remove`, `shelter animal remove`, `shelter adopter remove`, `shelter vaccine type remove`), ask the user to confirm. Do not proceed until the user explicitly says yes.

**Approval-step commands: submit only, then wait.** When the user asks to submit an adoption request, transfer request, or similar workflow request, only run the `submit` / `request` command. Do NOT automatically follow up with `approve`, `reject`, or `cancel`. Stop after creation and wait for an explicit instruction such as "批准" / "approve" / "拒绝" / "reject" / "取消" / "cancel" before running the next step.

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
- **Comments**: Javadoc on all public classes and methods; inline comments for non-trivial logic blocks
  - Comments go on the line **before** the code block they describe, never at the end of a code line
  - Group code into logical blocks and add one comment per block (not per line)
  - Explain **why** or **what the block achieves**, not just what the code literally does
  - Example blocks to comment: guard checks, business rule validations, domain delegation, side effects, persistence calls
- **Testing**: Two levels of tests, both run via `./gradlew test`:
  - **Unit tests**: JUnit, cover normal paths and edge cases; use `MockExplanationService` for AI module tests
  - **Integration tests**: tagged `@Tag("integration")`, spawn real `shelter` subprocesses via `ProcessBuilder`; isolated via `SHELTER_HOME` env var pointing to a `@TempDir` — the real `~/shelter` directory is never touched; 78 tests covering all UC-01 through UC-08 including error cases
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
- [docs/integration-test-plan.md](docs/integration-test-plan.md) — CLI integration test design and isolation strategy
- [docs/proposal/proposal-for-pdf.md](docs/proposal/proposal-for-pdf.md) — human-readable proposal
- [docs/diagram-class.mmd](docs/diagram-class.mmd) — class diagram (Mermaid source)
- [docs/diagram-layer.mmd](docs/diagram-layer.mmd) — layer architecture diagram
- [docs/Final_Project_Instructions.pdf](docs/Final_Project_Instructions.pdf) — original assignment instructions

---

## Git & Collaboration

See [CONTRIBUTING.md](CONTRIBUTING.md) for branch strategy, commit message format, and team responsibilities.
