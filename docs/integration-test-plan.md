# Integration Test Design — CLI End-to-End

**Date:** 2026-04-12  
**Branch:** `test/integration-cli`  
**Deadline context:** Project demo 2026-04-21

---

## Goal

Verify the full CLI stack end-to-end: Picocli parsing → Application layer → Service layer → CSV persistence → stdout output. Each test runs a real `shelter` subprocess and asserts on exit code, stdout, and (where applicable) CSV file content.

---

## Production Code Change (minimal)

`Main.java` reads a `SHELTER_HOME` environment variable. If set, it overrides the default `~/shelter` path. Existing behavior is unchanged when the variable is absent.

```java
String shelterHomeEnv = System.getenv("SHELTER_HOME");
Path shelterHome = shelterHomeEnv != null
        ? Path.of(shelterHomeEnv)
        : Path.of(System.getProperty("user.home"), "shelter");
new SystemStartupImpl(shelterHome).initialize();
```

---

## Test Infrastructure

### Package
`src/test/java/shelter/integration/`

### Abstract Base Class: `CliIntegrationTest`

Responsibilities:
- Provides `@TempDir Path shelterHome` — each test method gets a fresh, isolated directory
- `run(String... args)` — builds and executes a `shelter` subprocess with `SHELTER_HOME` set to the TempDir; returns a `RunResult` record containing `exitCode`, `stdout`, `stderr`
- Helper assertions: `assertExitCode(int)`, `assertOutputContains(String)`, `assertOutputDoesNotContain(String)`, `assertStdErrContains(String)`

### `RunResult` record
```
record RunResult(int exitCode, String stdout, String stderr)
```

### Subprocess mechanics
- `ProcessBuilder` with `environment().put("SHELTER_HOME", shelterHome.toString())`
- `inheritIO()` disabled; stdout and stderr captured separately
- Timeout: 10 seconds per invocation (guard against hangs)
- Binary located via `ProcessBuilder("shelter", ...)` — assumes the `shelter` script is on PATH (Gradle `installDist` task produces it)

---

## Test Scenarios (~30 cases)

### ShelterIntegrationTest
| Test | Asserts |
|------|---------|
| `register_validArgs_printsId` | exit 0, stdout contains shelter name |
| `list_noShelters_printsEmpty` | exit 0, stdout indicates no shelters |
| `list_afterRegister_showsShelter` | exit 0, shelter name in output |
| `register_missingName_printsError` | exit ≠ 0 or stderr non-empty |

### AnimalIntegrationTest
| Test | Asserts |
|------|---------|
| `admit_dog_printsAnimalId` | exit 0, stdout contains animal ID |
| `admit_cat_printsAnimalId` | exit 0 |
| `admit_rabbit_printsAnimalId` | exit 0 |
| `admit_other_printsAnimalId` | exit 0, speciesName preserved on `animal list` |
| `list_byShelter_filtersCorrectly` | only animals in given shelter shown |
| `admit_exceedsCapacity_printsError` | error message in output |
| `admit_missingShelter_printsError` | error message in output |

### AdopterIntegrationTest
| Test | Asserts |
|------|---------|
| `register_withAllPrefs_printsId` | exit 0 |
| `register_withoutAgePrefs_noDefault` | `animal list` and match not polluted by age 20 |
| `list_afterRegister_showsAdopter` | adopter name in output |

### AdoptionIntegrationTest
| Test | Asserts |
|------|---------|
| `submit_validPair_printsRequestId` | exit 0 |
| `approve_validRequest_printsConfirmation` | exit 0; subsequent `animal list` shows adopter |
| `reject_validRequest_printsConfirmation` | exit 0 |
| `cancel_validRequest_printsConfirmation` | exit 0 |
| `approve_nonExistentRequest_printsError` | error in output |
| `approve_alreadyAdopted_printsError` | error in output |

### TransferIntegrationTest
| Test | Asserts |
|------|---------|
| `request_validTransfer_printsRequestId` | exit 0 |
| `approve_validTransfer_animalMovedShelter` | `animal list --shelter <new>` shows animal |
| `reject_validTransfer_printsConfirmation` | exit 0 |

### MatchIntegrationTest
| Test | Asserts |
|------|---------|
| `matchAnimal_returnsRankedList` | exit 0, at least one result row |
| `matchAnimal_noAnimalsInShelter_printsEmpty` | exit 0, empty result message |
| `matchAdopter_returnsRankedList` | exit 0, at least one result row |
| `matchAnimal_adopterNoAgePrefs_allAnimalsEligible` | score not zero-penalised by age sentinel |

### VaccineIntegrationTest
| Test | Asserts |
|------|---------|
| `record_validVaccination_printsConfirmation` | exit 0 |
| `overdue_noVaccinations_printsOverdue` | exit 0, animal listed as overdue |
| `overdue_recentVaccination_notOverdue` | animal not listed as overdue |
| `vaccineType_addAndList_showsType` | exit 0, type name in output |

### AuditIntegrationTest
| Test | Asserts |
|------|---------|
| `log_afterAdmitAndAdopt_containsBothEntries` | audit output has ≥2 entries |

### CrossSessionIntegrationTest
| Test | Asserts |
|------|---------|
| `dataPersistedAcrossProcesses` | register shelter in process 1; new process 2 lists it |
| `otherAnimal_persistedAcrossProcesses` | admit Other in process 1; list in process 2 shows speciesName |

---

## What Is NOT Tested Here

- AI explanation output (non-deterministic; tested with MockExplanationService in unit tests)
- Exact score values from matching (covered in strategy unit tests)
- All CLI flag combinations (covered in unit tests)

---

## Running the Tests

```bash
# Build the CLI binary first
./gradlew installDist

# Run all tests including integration
./gradlew test
```

Integration tests are annotated `@Tag("integration")` so they can be run or excluded independently:

```bash
./gradlew test -Dgroups=integration      # only integration
./gradlew test -DexcludedGroups=integration  # skip integration
```

---

## File Layout

```
src/test/java/shelter/integration/
├── CliIntegrationTest.java          ← abstract base, RunResult, helpers
├── ShelterIntegrationTest.java
├── AnimalIntegrationTest.java
├── AdopterIntegrationTest.java
├── AdoptionIntegrationTest.java
├── TransferIntegrationTest.java
├── MatchIntegrationTest.java
├── VaccineIntegrationTest.java
├── AuditIntegrationTest.java
└── CrossSessionIntegrationTest.java
```
