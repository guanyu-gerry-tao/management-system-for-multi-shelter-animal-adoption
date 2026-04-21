# Multi-Shelter Animal Adoption Management System

**This system is designed to be operated by an AI agent.**
The user speaks in natural language. Claude Code interprets the intent and executes `shelter` CLI commands on their behalf.

## Getting Started

**Prerequisites:** Java 21+ (Gradle wrapper is included)

```bash
git clone https://github.com/guanyu-gerry-tao/management-system-for-multi-shelter-animal-adoption.git
cd management-system-for-multi-shelter-animal-adoption

./install.sh              # builds, creates ~/shelter/, symlinks `shelter` to /usr/local/bin
shelter --help
```

`./install.sh` does everything: checks Java 21+, builds via `./gradlew installDist`, creates `~/shelter/` (with `data/`, `CLAUDE.md`, `AGENTS.md`, `.claude/settings.json`), installs a `shelter` symlink under `/usr/local/bin` (prompts for sudo if needed), and drops you into a new shell at `~/shelter/`.

**To use the AI-agent experience** (natural language → `shelter` commands), run `claude` (Claude Code) or `codex` from inside `~/shelter/` after install. The AI agent will read `CLAUDE.md` / `AGENTS.md` and translate your intent into CLI calls.

**To use the CLI directly**, just run `shelter <subcommand>` from any directory — no agent required.

Run all tests:
```bash
./gradlew test
```

Integration tests spawn real `shelter` subprocesses via `ProcessBuilder`, isolated with `SHELTER_HOME` pointing to a `@TempDir` — the real `~/shelter/` is never touched.

## What This System Is

Animal shelters have a coordination problem. Dozens of animals need homes. Dozens of prospective adopters have different lifestyles, living spaces, and preferences. Animals move between shelters. Vaccines expire. Requests get approved, rejected, cancelled. Someone needs to keep track of all of it.

This system manages that entire workflow — across multiple shelters, for any number of animals and adopters — through a single CLI tool called `shelter`. It is built as a Java OOD project, but it is designed to be *driven by an AI agent*: the operator speaks in natural language, and Claude Code translates that into the right sequence of commands.

## What It Can Do

| | |
|---|---|
| **Shelters** | Register multiple shelters with name, location, and capacity |
| **Animals** | Admit dogs, cats, rabbits, or any other species; track breed, age, activity level, and species-specific traits |
| **Adopters** | Register with lifestyle context (living space, schedule) and preferences (species, breed, age range, vaccination requirements) |
| **Matching** | Score and rank animals for an adopter — or adopters for an animal — across six independent compatibility strategies; Claude generates a plain-language explanation of the result |
| **Adoptions** | Full request lifecycle: submit → approve/reject → cancel; adopted animals are locked to their new owner |
| **Transfers** | Move animals between shelters with the same approve/reject/cancel flow; capacity is enforced |
| **Vaccinations** | Record vaccines by type and date; flag overdue animals; manage a per-species vaccine catalog |
| **Audit** | Append-only log of every operation, with timestamp and actor |

## How It Works

A human says:

> *"Find a good match for the golden retriever in shelter 2, then submit an adoption request from Alice."*

Claude Code runs:

```bash
shelter shelter list                        # get shelter IDs
shelter animal list --shelter <id>          # find the dog's ID
shelter adopter list                        # find Alice's ID
shelter match adopter --animal <animal-id>  # confirm Alice ranks well
shelter adopt submit --adopter <alice-id> --animal <animal-id>
```

The `shelter` CLI is the only interface. All state is persisted to `~/shelter/data/` as CSV files. Every command is stateless — load, act, save.

**IDs are never known in advance. Always `list` first.**

## Command Overview

| Area | What you can do |
|---|---|
| Shelters | Register, update, remove shelters; list all |
| Animals | Admit animals (dog/cat/rabbit/other), update records, remove, list by shelter |
| Adopters | Register with lifestyle + preferences, update, remove, list all |
| Matching | Rank animals for an adopter; rank adopters for an animal |
| Adoptions | Submit requests, approve/reject/cancel, view status |
| Transfers | Request inter-shelter moves, approve/reject/cancel |
| Vaccinations | Record vaccines, check overdue, manage vaccine type catalog |
| Audit | View the full operation history |

## CLI Reference

### Shelter
```bash
shelter shelter list
shelter shelter register --name <name> --location <location> --capacity <n>
shelter shelter update --id <id> [--name <name>] [--location <location>] [--capacity <n>]
shelter shelter remove --id <id>
```

### Animal
```bash
shelter animal list [--shelter <id>]
shelter animal admit --species dog|cat|rabbit|other \
  --name <name> --breed <breed> --age <years> \
  --activity <LOW|MEDIUM|HIGH> --shelter <id> [species-specific flags]
shelter animal update --id <id> [--name <name>] [--activity <...>] [--neutered <true|false>]
shelter animal remove --id <id>
```

Species-specific flags for `admit`:
- `dog` → `[--size <SMALL|MEDIUM|LARGE>] [--neutered]`
- `cat` → `[--indoor] [--neutered]`
- `rabbit` → `[--fur <SHORT|LONG>]`
- `other` → `--species-name <e.g. fish>`

### Adopter
```bash
shelter adopter list
shelter adopter register --name <name> \
  --space <APARTMENT|HOUSE_NO_YARD|HOUSE_WITH_YARD> \
  --schedule <HOME_MOST_OF_DAY|AWAY_PART_OF_DAY|AWAY_MOST_OF_DAY> \
  [--species <DOG|CAT|RABBIT|OTHER>] [--breed <breed>] [--activity <LOW|MEDIUM|HIGH>] \
  [--requires-vaccinated <true|false>] [--min-age <n>] [--max-age <n>]
shelter adopter update --id <id> [same optional flags]
shelter adopter remove --id <id>
```

### Matching
```bash
shelter match animal  --adopter <id> --shelter <id>   # rank animals for an adopter
shelter match adopter --animal <id>                    # rank adopters for an animal
```

### Adoption
```bash
shelter adopt submit  --adopter <id> --animal <id>
shelter adopt approve --request <id>
shelter adopt reject  --request <id>
shelter adopt cancel  --request <id>
```

### Transfer
```bash
shelter transfer request --animal <id> --from <shelter-id> --to <shelter-id>
shelter transfer approve --request <id>
shelter transfer reject  --request <id>
shelter transfer cancel  --request <id>
```

### Vaccination
```bash
shelter vaccine record  --animal <id> --type <vaccine-type-name> --date <yyyy-mm-dd>
shelter vaccine overdue --animal <id>
shelter vaccine type list
shelter vaccine type add    --name <name> --species <DOG|CAT|RABBIT|OTHER> --days <n>
shelter vaccine type update --id <id> [--name <name>] [--species <...>] [--days <n>]
shelter vaccine type remove --id <id>
```

### Audit
```bash
shelter audit log
```


## Architecture

Five layers, each with a single responsibility:

```
Presentation Layer  ←  Picocli CLI (shelter animal admit ..., shelter adopt approve ...)
Application Layer   ←  orchestrates use cases across multiple services
Service Layer       ←  single-responsibility business operations
Strategy Layer      ←  pluggable scoring rules (IMatchingStrategy and implementations)
Domain Layer        ←  core entities (Animal, Shelter, Adopter, AdoptionRequest, ...)
```

**Matching** is powered by six composable strategies — `SpeciesPreferenceStrategy`, `BreedPreferenceStrategy`, `ActivityLevelStrategy`, `AgePreferenceStrategy`, `LifestyleCompatibilityStrategy`, `VaccinationPreferenceStrategy` — each independently scoreable and combinable. AI explanation runs *after* scoring and does not affect results.

**Animals** support four species: `Dog`, `Cat`, `Rabbit`, and `Other` (free-form species name).

## Project Structure

```
src/main/java/shelter/
  cli/          # Presentation layer — Picocli subcommands
  application/  # Application layer — use-case orchestrators
  service/      # Service layer — business operations
  strategy/     # Strategy layer — matching scoring rules
  domain/       # Domain layer — entities and value objects
  repository/   # CSV-backed persistence
  exception/    # Domain-specific exceptions
docs/
  use-cases.md              # Use-case list with method signatures
  integration-test-plan.md  # Integration test design
  diagram-class.mmd         # Class diagram (Mermaid)
  diagram-layer.mmd         # Layer architecture diagram
```


## Team

| Name | GitHub | Email |
|---|---|---|
| Guanyu (Gerry) Tao | [@guanyu-gerry-tao](https://github.com/guanyu-gerry-tao) | tao.gua@northeastern.edu |
| Yiying (Irene) Xie | [@Bestpart-Irene](https://github.com/Bestpart-Irene) | xie.yiyi@northeastern.edu |
| Yuxi (Molly) Ou | [@mollyouyuxi](https://github.com/mollyouyuxi) | ou.yux@northeastern.edu |

*CS 5004 Final Project · Northeastern University · Spring 2026*
