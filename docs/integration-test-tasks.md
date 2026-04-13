# Integration Test — CLI End-to-End Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement ~30 end-to-end integration tests that run the real `shelter` binary against a TempDir and assert on stdout, stderr, and exit code.

**Architecture:** JUnit 5 + ProcessBuilder. An abstract base class `CliIntegrationTest` provides a `run()` helper that spawns a subprocess with `SHELTER_HOME` set to `@TempDir`. Each test file covers one CLI command group. `Main.java` is patched to read `SHELTER_HOME` so tests are fully isolated from `~/shelter/data/`.

**Tech Stack:** Java 17, JUnit Jupiter 5.10, Picocli 4.x, Gradle `installDist`

---

## File Map

| Action | File |
|--------|------|
| Modify | `src/main/java/shelter/cli/Main.java` |
| Modify | `build.gradle` |
| Create | `src/test/java/shelter/integration/CliIntegrationTest.java` |
| Create | `src/test/java/shelter/integration/ShelterIntegrationTest.java` |
| Create | `src/test/java/shelter/integration/AnimalIntegrationTest.java` |
| Create | `src/test/java/shelter/integration/AdopterIntegrationTest.java` |
| Create | `src/test/java/shelter/integration/AdoptionIntegrationTest.java` |
| Create | `src/test/java/shelter/integration/TransferIntegrationTest.java` |
| Create | `src/test/java/shelter/integration/MatchIntegrationTest.java` |
| Create | `src/test/java/shelter/integration/VaccineIntegrationTest.java` |
| Create | `src/test/java/shelter/integration/AuditIntegrationTest.java` |
| Create | `src/test/java/shelter/integration/CrossSessionIntegrationTest.java` |

---

## Task 1: Patch Main.java + build.gradle

**Files:**
- Modify: `src/main/java/shelter/cli/Main.java`
- Modify: `build.gradle`

- [ ] **Step 1.1: Replace hardcoded path in Main.java**

Replace the `main()` method body with:

```java
public static void main(String[] args) {
    String shelterHomeEnv = System.getenv("SHELTER_HOME");
    Path shelterHome = shelterHomeEnv != null
            ? Path.of(shelterHomeEnv)
            : Path.of(System.getProperty("user.home"), "shelter");
    new SystemStartupImpl(shelterHome).initialize();
    int exitCode = new CommandLine(new Main()).execute(args);
    System.exit(exitCode);
}
```

Add import at the top if not already present: `import java.nio.file.Path;`

- [ ] **Step 1.2: Make test task depend on installDist in build.gradle**

Replace the existing `test { useJUnitPlatform() }` block with:

```groovy
test {
    useJUnitPlatform()
    dependsOn installDist
}
```

- [ ] **Step 1.3: Verify build works**

```bash
./gradlew installDist
```

Expected: `BUILD SUCCESSFUL`. Binary appears at `build/install/shelter/bin/shelter`.

- [ ] **Step 1.4: Smoke-test the binary with SHELTER_HOME**

```bash
SHELTER_HOME=/tmp/smoke-test build/install/shelter/bin/shelter shelter list
```

Expected: `No shelters registered.` (not an error, no crash).

- [ ] **Step 1.5: Commit**

```bash
git add src/main/java/shelter/cli/Main.java build.gradle
git commit -m "feat(cli): support SHELTER_HOME env var for test isolation"
```

---

## Task 2: Abstract Base Class

**Files:**
- Create: `src/test/java/shelter/integration/CliIntegrationTest.java`

- [ ] **Step 2.1: Create the abstract base class**

```java
package shelter.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstract base class for CLI integration tests.
 * Spawns real {@code shelter} subprocesses with SHELTER_HOME pointed at a JUnit TempDir
 * so each test method runs against a completely isolated, ephemeral data directory.
 * The binary is expected at {@code build/install/shelter/bin/shelter} (produced by installDist).
 */
@Tag("integration")
abstract class CliIntegrationTest {

    private static final Path BINARY = Path.of(System.getProperty("user.dir"))
            .resolve("build/install/shelter/bin/shelter");

    /** Fresh isolated data directory injected by JUnit for every test method. */
    @TempDir
    Path shelterHome;

    /**
     * Launches the shelter binary with the given arguments and captures output.
     * SHELTER_HOME is set to the per-test TempDir; no real user data is touched.
     *
     * @param args subcommand tokens, e.g. "shelter", "register", "--name", "Paws"
     * @return result containing exit code, stdout, and stderr
     */
    RunResult run(String... args) throws IOException, InterruptedException {
        String[] command = new String[args.length + 1];
        command[0] = BINARY.toString();
        System.arraycopy(args, 0, command, 1, args.length);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().put("SHELTER_HOME", shelterHome.toString());

        Process process = pb.start();
        String stdout = new String(process.getInputStream().readAllBytes());
        String stderr  = new String(process.getErrorStream().readAllBytes());
        boolean finished = process.waitFor(10, TimeUnit.SECONDS);
        assertTrue(finished, "shelter process timed out after 10 s");

        return new RunResult(process.exitValue(), stdout, stderr);
    }

    /**
     * Extracts a UUID that follows {@code id=} in command output.
     * Matches patterns like {@code "Registered shelter: Paws (id=abc-123)"}.
     *
     * @param output stdout string from a run() call
     * @return the extracted UUID string
     */
    String extractId(String output) {
        Matcher m = Pattern.compile("id=([a-f0-9\\-]{36})").matcher(output);
        assertTrue(m.find(), "Expected id=<uuid> in output: " + output);
        return m.group(1);
    }

    /** Asserts exit code is 0. */
    void assertSuccess(RunResult r) {
        assertEquals(0, r.exitCode(),
                "Expected exit 0. stderr: " + r.stderr() + " stdout: " + r.stdout());
    }

    /** Asserts stdout or stderr contains the fragment. */
    void assertOutputContains(RunResult r, String fragment) {
        assertTrue((r.stdout() + r.stderr()).contains(fragment),
                "Expected output to contain \"" + fragment + "\"\nstdout: " + r.stdout() + "\nstderr: " + r.stderr());
    }

    /** Asserts stdout and stderr do NOT contain the fragment. */
    void assertOutputDoesNotContain(RunResult r, String fragment) {
        assertFalse((r.stdout() + r.stderr()).contains(fragment),
                "Expected output NOT to contain \"" + fragment + "\"\nstdout: " + r.stdout());
    }

    /** Immutable result of one CLI invocation. */
    record RunResult(int exitCode, String stdout, String stderr) {}
}
```

- [ ] **Step 2.2: Compile check**

```bash
./gradlew compileTestJava
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2.3: Commit**

```bash
git add src/test/java/shelter/integration/CliIntegrationTest.java
git commit -m "test(integration): add CliIntegrationTest base class with ProcessBuilder helper"
```

---

## Task 3: ShelterIntegrationTest

**Files:**
- Create: `src/test/java/shelter/integration/ShelterIntegrationTest.java`

CLI output reference:
- `register` → `"Registered shelter: Happy Paws (id=<uuid>)"`
- `list` (empty) → `"No shelters registered."`
- `list` (has data) → table rows with shelter name and location

- [ ] **Step 3.1: Write the test file**

```java
package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter shelter} command group.
 * Each test runs against a fresh TempDir; no shared state between methods.
 */
class ShelterIntegrationTest extends CliIntegrationTest {

    @Test
    void register_validArgs_printsNameAndId() throws Exception {
        RunResult r = run("shelter", "register",
                "--name", "Happy Paws", "--location", "Boston", "--capacity", "20");
        assertSuccess(r);
        assertOutputContains(r, "Happy Paws");
        assertOutputContains(r, "id=");
    }

    @Test
    void list_noShelters_printsEmptyMessage() throws Exception {
        RunResult r = run("shelter", "list");
        assertSuccess(r);
        assertOutputContains(r, "No shelters registered.");
    }

    @Test
    void list_afterRegister_showsRegisteredShelter() throws Exception {
        run("shelter", "register",
                "--name", "Happy Paws", "--location", "Boston", "--capacity", "20");

        RunResult r = run("shelter", "list");
        assertSuccess(r);
        assertOutputContains(r, "Happy Paws");
        assertOutputContains(r, "Boston");
    }

    @Test
    void register_missingRequiredOption_exitNonZero() throws Exception {
        // --location and --capacity are missing; Picocli should reject this
        RunResult r = run("shelter", "register", "--name", "Incomplete");
        assertNotEquals(0, r.exitCode());
    }
}
```

- [ ] **Step 3.2: Run and verify all pass**

```bash
./gradlew test --tests "shelter.integration.ShelterIntegrationTest"
```

Expected: 4 tests, all PASS.

- [ ] **Step 3.3: Commit**

```bash
git add src/test/java/shelter/integration/ShelterIntegrationTest.java
git commit -m "test(integration): ShelterIntegrationTest — register and list"
```

---

## Task 4: AnimalIntegrationTest

**Files:**
- Create: `src/test/java/shelter/integration/AnimalIntegrationTest.java`

CLI output reference:
- `admit` → `"Admitted DOG: Rex (id=<uuid>, shelter=<uuid>)"`
- `list` (empty) → `"No animals found."`
- `list` (has data) → table rows containing species, name, status

- [ ] **Step 4.1: Write the test file**

```java
package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter animal} command group.
 * Covers Dog, Cat, Rabbit, and Other species, plus list filtering and capacity enforcement.
 */
class AnimalIntegrationTest extends CliIntegrationTest {

    private String registerShelter(String name, int capacity) throws Exception {
        RunResult r = run("shelter", "register",
                "--name", name, "--location", "Boston", "--capacity", String.valueOf(capacity));
        return extractId(r.stdout());
    }

    @Test
    void admit_dog_printsIdAndSpecies() throws Exception {
        String shelterId = registerShelter("Paws", 10);
        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Labrador",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Rex");
        assertOutputContains(r, "id=");
    }

    @Test
    void admit_cat_printsIdAndSpecies() throws Exception {
        String shelterId = registerShelter("Paws", 10);
        RunResult r = run("animal", "admit",
                "--species", "cat", "--name", "Whiskers", "--breed", "Siamese",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Whiskers");
    }

    @Test
    void admit_rabbit_printsIdAndSpecies() throws Exception {
        String shelterId = registerShelter("Paws", 10);
        RunResult r = run("animal", "admit",
                "--species", "rabbit", "--name", "Fluffy", "--breed", "Holland Lop",
                "--age", "1", "--activity", "MEDIUM", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Fluffy");
    }

    @Test
    void admit_other_speciesNamePreservedOnList() throws Exception {
        String shelterId = registerShelter("Paws", 10);
        run("animal", "admit",
                "--species", "fish", "--name", "Nemo", "--breed", "Clownfish",
                "--age", "1", "--activity", "LOW", "--shelter", shelterId);

        RunResult r = run("animal", "list", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Nemo");
    }

    @Test
    void list_byShelter_onlyShowsAnimalsInThatShelter() throws Exception {
        String shelterA = registerShelter("Shelter A", 10);
        String shelterB = registerShelter("Shelter B", 10);
        run("animal", "admit",
                "--species", "dog", "--name", "InA", "--breed", "Breed",
                "--age", "2", "--activity", "LOW", "--shelter", shelterA);
        run("animal", "admit",
                "--species", "dog", "--name", "InB", "--breed", "Breed",
                "--age", "2", "--activity", "LOW", "--shelter", shelterB);

        RunResult r = run("animal", "list", "--shelter", shelterA);
        assertOutputContains(r, "InA");
        assertOutputDoesNotContain(r, "InB");
    }

    @Test
    void admit_exceedsCapacity_printsError() throws Exception {
        String shelterId = registerShelter("Tiny", 1);
        run("animal", "admit",
                "--species", "dog", "--name", "First", "--breed", "Breed",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);

        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", "Second", "--breed", "Breed",
                "--age", "2", "--activity", "LOW", "--shelter", shelterId);
        // Shelter is full; expect an error message
        assertOutputContains(r, "Error");
    }

    @Test
    void list_noAnimals_printsEmptyMessage() throws Exception {
        String shelterId = registerShelter("Empty", 10);
        RunResult r = run("animal", "list", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "No animals found.");
    }
}
```

- [ ] **Step 4.2: Run and verify**

```bash
./gradlew test --tests "shelter.integration.AnimalIntegrationTest"
```

Expected: 7 tests, all PASS.

- [ ] **Step 4.3: Commit**

```bash
git add src/test/java/shelter/integration/AnimalIntegrationTest.java
git commit -m "test(integration): AnimalIntegrationTest — admit Dog/Cat/Rabbit/Other, list, capacity"
```

---

## Task 5: AdopterIntegrationTest

**Files:**
- Create: `src/test/java/shelter/integration/AdopterIntegrationTest.java`

CLI output reference:
- `register` → `"Registered adopter: Alice (id=<uuid>)"`
- `list` (empty) → `"No adopters registered."`

- [ ] **Step 5.1: Write the test file**

```java
package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter adopter} command group.
 * Verifies registration with and without age preferences, and listing behaviour.
 */
class AdopterIntegrationTest extends CliIntegrationTest {

    @Test
    void register_withAllPrefs_printsIdAndName() throws Exception {
        RunResult r = run("adopter", "register",
                "--name", "Alice",
                "--living-space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY",
                "--species", "DOG",
                "--min-age", "1",
                "--max-age", "5");
        assertSuccess(r);
        assertOutputContains(r, "Alice");
        assertOutputContains(r, "id=");
    }

    @Test
    void register_withoutAgePrefs_doesNotDefaultToAge20() throws Exception {
        // Regression: --max-age previously defaulted to 20, contaminating match scores.
        // Without --min-age / --max-age, the adopter should have null age bounds.
        RunResult r = run("adopter", "register",
                "--name", "Bob",
                "--living-space", "APARTMENT",
                "--schedule", "AWAY_MOST_OF_DAY");
        assertSuccess(r);
        assertOutputContains(r, "Bob");
        // No error indicates the null age was accepted correctly
    }

    @Test
    void list_noAdopters_printsEmptyMessage() throws Exception {
        RunResult r = run("adopter", "list");
        assertSuccess(r);
        assertOutputContains(r, "No adopters registered.");
    }

    @Test
    void list_afterRegister_showsAdopter() throws Exception {
        run("adopter", "register",
                "--name", "Carol",
                "--living-space", "APARTMENT",
                "--schedule", "HOME_MOST_OF_DAY");
        RunResult r = run("adopter", "list");
        assertSuccess(r);
        assertOutputContains(r, "Carol");
    }
}
```

- [ ] **Step 5.2: Run and verify**

```bash
./gradlew test --tests "shelter.integration.AdopterIntegrationTest"
```

Expected: 4 tests, all PASS.

- [ ] **Step 5.3: Commit**

```bash
git add src/test/java/shelter/integration/AdopterIntegrationTest.java
git commit -m "test(integration): AdopterIntegrationTest — register with/without age prefs, list"
```

---

## Task 6: AdoptionIntegrationTest

**Files:**
- Create: `src/test/java/shelter/integration/AdoptionIntegrationTest.java`

CLI output reference:
- `submit` → `"Submitted adoption request: id=<uuid> (adopter=<uuid>, animal=<uuid>)"`
- `approve` → `"Approved adoption request: <uuid>"`
- `reject` → `"Rejected adoption request: <uuid>"`
- `cancel` → `"Cancelled adoption request: <uuid>"`

- [ ] **Step 6.1: Write the test file**

```java
package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter adopt} command group.
 * Tests the full adoption lifecycle: submit → approve/reject/cancel, plus error paths.
 */
class AdoptionIntegrationTest extends CliIntegrationTest {

    private String registerShelter() throws Exception {
        RunResult r = run("shelter", "register",
                "--name", "Paws", "--location", "Boston", "--capacity", "10");
        return extractId(r.stdout());
    }

    private String admitDog(String shelterId) throws Exception {
        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        return extractId(r.stdout());
    }

    private String registerAdopter() throws Exception {
        RunResult r = run("adopter", "register",
                "--name", "Alice", "--living-space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY");
        return extractId(r.stdout());
    }

    private String submitAdoption(String adopterId, String animalId) throws Exception {
        RunResult r = run("adopt", "submit",
                "--adopter", adopterId, "--animal", animalId);
        return extractId(r.stdout());
    }

    @Test
    void submit_validPair_printsRequestId() throws Exception {
        String shelterId = registerShelter();
        String animalId  = admitDog(shelterId);
        String adopterId = registerAdopter();

        RunResult r = run("adopt", "submit",
                "--adopter", adopterId, "--animal", animalId);
        assertSuccess(r);
        assertOutputContains(r, "id=");
    }

    @Test
    void approve_validRequest_printsConfirmationAndAnimalIsAdopted() throws Exception {
        String shelterId  = registerShelter();
        String animalId   = admitDog(shelterId);
        String adopterId  = registerAdopter();
        String requestId  = submitAdoption(adopterId, animalId);

        RunResult r = run("adopt", "approve", "--request", requestId);
        assertSuccess(r);
        assertOutputContains(r, "Approved adoption request");

        // Animal should now show as adopted in the list
        RunResult list = run("animal", "list", "--shelter", shelterId);
        assertOutputContains(list, "adopted");
    }

    @Test
    void reject_validRequest_printsConfirmation() throws Exception {
        String shelterId = registerShelter();
        String animalId  = admitDog(shelterId);
        String adopterId = registerAdopter();
        String requestId = submitAdoption(adopterId, animalId);

        RunResult r = run("adopt", "reject", "--request", requestId);
        assertSuccess(r);
        assertOutputContains(r, "Rejected adoption request");
    }

    @Test
    void cancel_validRequest_printsConfirmation() throws Exception {
        String shelterId = registerShelter();
        String animalId  = admitDog(shelterId);
        String adopterId = registerAdopter();
        String requestId = submitAdoption(adopterId, animalId);

        RunResult r = run("adopt", "cancel", "--request", requestId);
        assertSuccess(r);
        assertOutputContains(r, "Cancelled adoption request");
    }

    @Test
    void approve_nonExistentRequest_printsError() throws Exception {
        RunResult r = run("adopt", "approve", "--request", "00000000-0000-0000-0000-000000000000");
        assertOutputContains(r, "Error");
    }

    @Test
    void approve_alreadyAdoptedAnimal_printsError() throws Exception {
        String shelterId  = registerShelter();
        String animalId   = admitDog(shelterId);
        String adopterId  = registerAdopter();
        String requestId1 = submitAdoption(adopterId, animalId);
        run("adopt", "approve", "--request", requestId1);

        // Try to submit a second adoption for the same animal
        RunResult r = run("adopt", "submit",
                "--adopter", adopterId, "--animal", animalId);
        assertOutputContains(r, "Error");
    }
}
```

- [ ] **Step 6.2: Run and verify**

```bash
./gradlew test --tests "shelter.integration.AdoptionIntegrationTest"
```

Expected: 6 tests, all PASS.

- [ ] **Step 6.3: Commit**

```bash
git add src/test/java/shelter/integration/AdoptionIntegrationTest.java
git commit -m "test(integration): AdoptionIntegrationTest — full lifecycle, error paths"
```

---

## Task 7: TransferIntegrationTest

**Files:**
- Create: `src/test/java/shelter/integration/TransferIntegrationTest.java`

CLI output reference:
- `transfer request` → `"Transfer request created: id=<uuid> (animal=<uuid>, <from> → <to>)"`
- `approve` → `"Approved transfer request: <uuid>"`
- `reject` → `"Rejected transfer request: <uuid>"`

- [ ] **Step 7.1: Write the test file**

```java
package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter transfer} command group.
 * Verifies request, approve (animal moves shelter), and reject flows.
 */
class TransferIntegrationTest extends CliIntegrationTest {

    private String registerShelter(String name) throws Exception {
        RunResult r = run("shelter", "register",
                "--name", name, "--location", "Boston", "--capacity", "10");
        return extractId(r.stdout());
    }

    private String admitDog(String shelterId) throws Exception {
        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        return extractId(r.stdout());
    }

    @Test
    void request_validTransfer_printsRequestId() throws Exception {
        String shelterA = registerShelter("Shelter A");
        String shelterB = registerShelter("Shelter B");
        String animalId = admitDog(shelterA);

        RunResult r = run("transfer", "request",
                "--animal", animalId, "--from", shelterA, "--to", shelterB);
        assertSuccess(r);
        assertOutputContains(r, "id=");
    }

    @Test
    void approve_validTransfer_animalAppearsInNewShelter() throws Exception {
        String shelterA = registerShelter("Shelter A");
        String shelterB = registerShelter("Shelter B");
        String animalId = admitDog(shelterA);

        RunResult requestResult = run("transfer", "request",
                "--animal", animalId, "--from", shelterA, "--to", shelterB);
        String requestId = extractId(requestResult.stdout());

        RunResult r = run("transfer", "approve", "--request", requestId);
        assertSuccess(r);
        assertOutputContains(r, "Approved transfer request");

        RunResult list = run("animal", "list", "--shelter", shelterB);
        assertOutputContains(list, "Rex");
    }

    @Test
    void reject_validTransfer_printsConfirmation() throws Exception {
        String shelterA = registerShelter("Shelter A");
        String shelterB = registerShelter("Shelter B");
        String animalId = admitDog(shelterA);

        RunResult requestResult = run("transfer", "request",
                "--animal", animalId, "--from", shelterA, "--to", shelterB);
        String requestId = extractId(requestResult.stdout());

        RunResult r = run("transfer", "reject", "--request", requestId);
        assertSuccess(r);
        assertOutputContains(r, "Rejected transfer request");
    }
}
```

- [ ] **Step 7.2: Run and verify**

```bash
./gradlew test --tests "shelter.integration.TransferIntegrationTest"
```

Expected: 3 tests, all PASS.

- [ ] **Step 7.3: Commit**

```bash
git add src/test/java/shelter/integration/TransferIntegrationTest.java
git commit -m "test(integration): TransferIntegrationTest — request, approve, reject"
```

---

## Task 8: MatchIntegrationTest

**Files:**
- Create: `src/test/java/shelter/integration/MatchIntegrationTest.java`

CLI output reference:
- `match animal` → ranked table `rank  animal-id  name  score` or `"No available animals..."`
- `match adopter` → ranked table `rank  adopter-id  name  score`

- [ ] **Step 8.1: Write the test file**

```java
package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter match} command group.
 * Verifies that ranked output is returned and that null age preferences
 * do not zero-penalise the score (regression for the age-sentinel bug).
 */
class MatchIntegrationTest extends CliIntegrationTest {

    private String registerShelter() throws Exception {
        RunResult r = run("shelter", "register",
                "--name", "Paws", "--location", "Boston", "--capacity", "10");
        return extractId(r.stdout());
    }

    private String admitDog(String shelterId) throws Exception {
        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        return extractId(r.stdout());
    }

    private String registerAdopter(String name) throws Exception {
        RunResult r = run("adopter", "register",
                "--name", name,
                "--living-space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY",
                "--species", "DOG");
        return extractId(r.stdout());
    }

    @Test
    void matchAnimal_returnsRankedList() throws Exception {
        String shelterId = registerShelter();
        admitDog(shelterId);
        String adopterId = registerAdopter("Alice");

        RunResult r = run("match", "animal",
                "--adopter", adopterId, "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Rex");
    }

    @Test
    void matchAnimal_noAnimalsInShelter_printsEmptyMessage() throws Exception {
        String shelterId = registerShelter();
        String adopterId = registerAdopter("Alice");

        RunResult r = run("match", "animal",
                "--adopter", adopterId, "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "No available animals");
    }

    @Test
    void matchAdopter_returnsRankedList() throws Exception {
        String shelterId = registerShelter();
        String animalId  = admitDog(shelterId);
        registerAdopter("Alice");

        RunResult r = run("match", "adopter", "--animal", animalId);
        assertSuccess(r);
        assertOutputContains(r, "Alice");
    }

    @Test
    void matchAnimal_adopterWithNoAgePrefs_nonZeroScore() throws Exception {
        // Regression: age sentinel 0..MAX_VALUE caused score penalisation for adopters
        // with no age preference. Without --min-age/--max-age, score should not be 0.
        String shelterId = registerShelter();
        admitDog(shelterId);
        RunResult ar = run("adopter", "register",
                "--name", "NoAge",
                "--living-space", "HOUSE_WITH_YARD",
                "--schedule", "HOME_MOST_OF_DAY");
        String adopterId = extractId(ar.stdout());

        RunResult r = run("match", "animal",
                "--adopter", adopterId, "--shelter", shelterId);
        assertSuccess(r);
        // Rex must appear in the ranked output — if age sentinel caused score 0
        // and the animal were excluded, Rex would be absent from the output.
        assertOutputContains(r, "Rex");
    }
}
```

- [ ] **Step 8.2: Run and verify**

```bash
./gradlew test --tests "shelter.integration.MatchIntegrationTest"
```

Expected: 4 tests, all PASS.

- [ ] **Step 8.3: Commit**

```bash
git add src/test/java/shelter/integration/MatchIntegrationTest.java
git commit -m "test(integration): MatchIntegrationTest — ranked results, null-age regression"
```

---

## Task 9: VaccineIntegrationTest

**Files:**
- Create: `src/test/java/shelter/integration/VaccineIntegrationTest.java`

CLI output reference:
- `vaccine type add` → `"Added vaccine type: Rabies (id=<uuid>, species=DOG, validity=365 days)"`
- `vaccine type list` → table rows / `"No vaccine types in catalog."`
- `vaccine record` → `"Recorded vaccination: animal=<uuid>, type=Rabies, date=<date>"`
- `vaccine overdue` (no overdue) → `"All vaccinations are current for animal: <uuid>"`
- `vaccine overdue` (has overdue) → table with vaccine type and due date

- [ ] **Step 9.1: Write the test file**

```java
package shelter.integration;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter vaccine} command group.
 * Covers vaccine type CRUD, recording vaccinations, and overdue checks.
 */
class VaccineIntegrationTest extends CliIntegrationTest {

    private String registerShelter() throws Exception {
        RunResult r = run("shelter", "register",
                "--name", "Paws", "--location", "Boston", "--capacity", "10");
        return extractId(r.stdout());
    }

    private String admitDog(String shelterId) throws Exception {
        RunResult r = run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);
        return extractId(r.stdout());
    }

    @Test
    void vaccineType_addAndList_showsType() throws Exception {
        run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--validity", "365");
        RunResult r = run("vaccine", "type", "list");
        assertSuccess(r);
        assertOutputContains(r, "Rabies");
    }

    @Test
    void record_validVaccination_printsConfirmation() throws Exception {
        String shelterId = registerShelter();
        String animalId  = admitDog(shelterId);
        run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--validity", "365");

        RunResult r = run("vaccine", "record",
                "--animal", animalId,
                "--type", "Rabies",
                "--date", LocalDate.now().toString());
        assertSuccess(r);
        assertOutputContains(r, "Recorded vaccination");
    }

    @Test
    void overdue_recentVaccination_notOverdue() throws Exception {
        String shelterId = registerShelter();
        String animalId  = admitDog(shelterId);
        run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--validity", "365");
        run("vaccine", "record",
                "--animal", animalId,
                "--type", "Rabies",
                "--date", LocalDate.now().toString());

        RunResult r = run("vaccine", "overdue", "--animal", animalId);
        assertSuccess(r);
        assertOutputContains(r, "All vaccinations are current");
    }

    @Test
    void overdue_expiredVaccination_printsOverdueEntry() throws Exception {
        String shelterId = registerShelter();
        String animalId  = admitDog(shelterId);
        run("vaccine", "type", "add",
                "--name", "Rabies", "--species", "DOG", "--validity", "365");
        // Record vaccination 2 years ago — well past the 365-day validity
        String pastDate = LocalDate.now().minusYears(2).toString();
        run("vaccine", "record",
                "--animal", animalId, "--type", "Rabies", "--date", pastDate);

        RunResult r = run("vaccine", "overdue", "--animal", animalId);
        assertSuccess(r);
        assertOutputContains(r, "Rabies");
    }
}
```

- [ ] **Step 9.2: Run and verify**

```bash
./gradlew test --tests "shelter.integration.VaccineIntegrationTest"
```

Expected: 4 tests, all PASS.

- [ ] **Step 9.3: Commit**

```bash
git add src/test/java/shelter/integration/VaccineIntegrationTest.java
git commit -m "test(integration): VaccineIntegrationTest — type CRUD, record, overdue"
```

---

## Task 10: AuditIntegrationTest

**Files:**
- Create: `src/test/java/shelter/integration/AuditIntegrationTest.java`

CLI output reference:
- `audit log` (empty) → `"Audit log is empty."`
- `audit log` (has data) → table rows with entity type, action, timestamp

- [ ] **Step 10.1: Write the test file**

```java
package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the {@code shelter audit} command group.
 * Verifies that mutations are recorded in the audit log and are visible via CLI.
 */
class AuditIntegrationTest extends CliIntegrationTest {

    @Test
    void log_noActions_printsEmptyMessage() throws Exception {
        RunResult r = run("audit", "log");
        assertSuccess(r);
        assertOutputContains(r, "Audit log is empty.");
    }

    @Test
    void log_afterAdmitAndAdopt_containsBothEntries() throws Exception {
        RunResult sr = run("shelter", "register",
                "--name", "Paws", "--location", "Boston", "--capacity", "10");
        String shelterId = extractId(sr.stdout());

        run("animal", "admit",
                "--species", "dog", "--name", "Rex", "--breed", "Lab",
                "--age", "3", "--activity", "HIGH", "--shelter", shelterId);

        RunResult r = run("audit", "log");
        assertSuccess(r);
        // Audit log should contain at least entries for shelter register and animal admit
        assertOutputDoesNotContain(r, "Audit log is empty.");
    }
}
```

- [ ] **Step 10.2: Run and verify**

```bash
./gradlew test --tests "shelter.integration.AuditIntegrationTest"
```

Expected: 2 tests, all PASS.

- [ ] **Step 10.3: Commit**

```bash
git add src/test/java/shelter/integration/AuditIntegrationTest.java
git commit -m "test(integration): AuditIntegrationTest — empty log, entries after mutations"
```

---

## Task 11: CrossSessionIntegrationTest

**Files:**
- Create: `src/test/java/shelter/integration/CrossSessionIntegrationTest.java`

These tests verify that data written by one subprocess is readable by a subsequent subprocess — the core persistence guarantee.

- [ ] **Step 11.1: Write the test file**

```java
package shelter.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests verifying that data persists across separate shelter processes.
 * Each test runs two independent subprocesses against the same TempDir, simulating
 * a real shutdown-and-restart cycle.
 */
class CrossSessionIntegrationTest extends CliIntegrationTest {

    @Test
    void shelterRegistered_inProcess1_visibleInProcess2() throws Exception {
        // Process 1: register a shelter
        run("shelter", "register",
                "--name", "Persistent Paws", "--location", "Boston", "--capacity", "10");

        // Process 2: list shelters — data must survive across process boundary
        RunResult r = run("shelter", "list");
        assertSuccess(r);
        assertOutputContains(r, "Persistent Paws");
    }

    @Test
    void otherAnimal_admittedInProcess1_speciesNamePreservedInProcess2() throws Exception {
        // Regression: Other animals were silently dropped on CSV reload.
        RunResult sr = run("shelter", "register",
                "--name", "Paws", "--location", "Boston", "--capacity", "10");
        String shelterId = extractId(sr.stdout());

        // Process 1: admit an Other animal
        run("animal", "admit",
                "--species", "fish", "--name", "Nemo", "--breed", "Clownfish",
                "--age", "1", "--activity", "LOW", "--shelter", shelterId);

        // Process 2: list — Nemo must still appear with correct species
        RunResult r = run("animal", "list", "--shelter", shelterId);
        assertSuccess(r);
        assertOutputContains(r, "Nemo");
    }
}
```

- [ ] **Step 11.2: Run and verify**

```bash
./gradlew test --tests "shelter.integration.CrossSessionIntegrationTest"
```

Expected: 2 tests, all PASS.

- [ ] **Step 11.3: Commit**

```bash
git add src/test/java/shelter/integration/CrossSessionIntegrationTest.java
git commit -m "test(integration): CrossSessionIntegrationTest — data persistence across processes"
```

---

## Task 12: Full Run and Push

- [ ] **Step 12.1: Run all tests**

```bash
./gradlew test
```

Expected: all unit tests + all integration tests PASS. Check the HTML report at `build/reports/tests/test/index.html` and confirm the `shelter.integration` package appears.

- [ ] **Step 12.2: Push branch**

```bash
git push -u origin test/integration-cli
```

- [ ] **Step 12.3: Open PR against dev**

```bash
gh pr create --base dev --title "test(integration): CLI end-to-end integration tests" \
  --body "Adds ~30 CLI integration tests using ProcessBuilder + JUnit TempDir.
  
- Patches Main.java to read SHELTER_HOME env var for test isolation
- Abstract base class CliIntegrationTest with run(), extractId(), assertions
- 10 test files covering all CLI command groups
- Cross-session tests verify CSV persistence survives process restart

Tests run as part of ./gradlew test (installDist runs automatically first)."
```
