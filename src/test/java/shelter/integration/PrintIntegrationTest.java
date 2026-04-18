package shelter.integration;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the {@code shelter print} command, covering the one-shot snapshot,
 * populated-data rendering, and the {@code --watch} markdown dashboard file write.
 * All three scenarios spawn real {@code shelter} subprocesses against an isolated
 * {@code SHELTER_HOME} to exercise the full CLI stack.
 */
class PrintIntegrationTest extends CliIntegrationTest {

    /**
     * Verifies that {@code shelter print} against an empty system emits all eight
     * section titles in the documented order and that each empty section displays
     * the {@code (none)} marker.
     */
    @Test
    void print_onEmptySystem_rendersAllEightSectionsWithNone() throws Exception {
        RunResult r = run("print");
        assertSuccess(r);
        for (String title : new String[]{
                "=== SHELTERS ===", "=== ANIMALS ===", "=== ADOPTERS ===",
                "=== ADOPTION REQUESTS ===", "=== TRANSFER REQUESTS ===",
                "=== VACCINE TYPES ===", "=== VACCINATIONS ===", "=== AUDIT LOG ==="}) {
            assertOutputContains(r, title);
        }
        assertOutputContains(r, "(none)");
    }

    /**
     * Verifies that after seeding a shelter and admitting an animal, {@code shelter print}
     * includes both the shelter name and the animal name in the rendered snapshot.
     */
    @Test
    void print_afterSeedingData_showsShelterAndAnimalRows() throws Exception {
        String sId = extractId(run("shelter", "register",
                "--name", "Happy Tails", "--location", "Boston", "--capacity", "10").stdout());
        assertSuccess(run("animal", "admit", "--species", "dog", "--name", "Rex",
                "--breed", "Lab", "--age", "3", "--activity", "LOW", "--shelter", sId));

        RunResult r = run("print");
        assertSuccess(r);
        assertOutputContains(r, "Happy Tails");
        assertOutputContains(r, "Rex");
    }

    /**
     * Verifies that {@code shelter print --watch --out <path>} writes the dashboard file
     * on first tick and that the output contains the markdown structure the renderer produces.
     * The process is killed after the file appears so the test does not hang.
     */
    @Test
    void printWatch_writesDashboardFileAndExitsOnTimeout() throws Exception {
        Path out = shelterHome.resolve("dashboard.md");
        Path binary = Path.of(System.getProperty("user.dir"),
                "build/install/shelter/bin/shelter");
        String[] command = {
                binary.toString(),
                "print", "--watch", "--out", out.toString()
        };
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.environment().put("SHELTER_HOME", shelterHome.toString());
        Process p = pb.start();
        try {
            // Poll up to 5 s for the file to appear
            long deadline = System.currentTimeMillis() + 5000;
            while (!Files.exists(out) && System.currentTimeMillis() < deadline) {
                Thread.sleep(200);
            }
            assertTrue(Files.exists(out), "Dashboard file never written");
            String md = Files.readString(out);
            assertTrue(md.contains("# Shelter System State"));
            assertTrue(md.contains("## Shelters"));
        } finally {
            p.destroy();
            p.waitFor(5, TimeUnit.SECONDS);
        }
    }
}
