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
 * Abstract base class for CLI end-to-end integration tests.
 * Spawns real {@code shelter} subprocesses with {@code SHELTER_HOME} pointed at a JUnit
 * {@code @TempDir} so each test method runs against a completely isolated, ephemeral data
 * directory. The binary is expected at {@code build/install/shelter/bin/shelter},
 * which is produced by the Gradle {@code installDist} task.
 */
@Tag("integration")
abstract class CliIntegrationTest {

    private static final Path BINARY = Path.of(System.getProperty("user.dir"))
            .resolve("build/install/shelter/bin/shelter");

    /** Fresh isolated data directory injected by JUnit for every test method. */
    @TempDir
    Path shelterHome;

    /**
     * Launches the {@code shelter} binary with the given arguments and captures all output.
     * {@code SHELTER_HOME} is set to the per-test {@code @TempDir}; the real {@code ~/shelter}
     * directory is never touched.
     *
     * @param args subcommand tokens, e.g. {@code "shelter", "register", "--name", "Paws"}
     * @return a {@link RunResult} containing exit code, stdout, and stderr
     * @throws IOException          if the process cannot be started
     * @throws InterruptedException if the thread is interrupted while waiting
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
     * Extracts a UUID that follows {@code id=} anywhere in the given output string.
     * Matches CLI output patterns such as {@code "Registered shelter: Paws (id=abc-123)"}.
     *
     * @param output stdout string from a {@link #run} call
     * @return the extracted UUID string
     */
    String extractId(String output) {
        Matcher m = Pattern.compile("id=([a-f0-9\\-]{36})").matcher(output);
        assertTrue(m.find(), "Expected id=<uuid> in output: " + output);
        return m.group(1);
    }

    /**
     * Asserts that the process exited with code 0.
     * Includes stdout and stderr in the failure message for easier debugging.
     *
     * @param r the result to check
     */
    void assertSuccess(RunResult r) {
        assertEquals(0, r.exitCode(),
                "Expected exit 0. stderr: " + r.stderr() + " stdout: " + r.stdout());
    }

    /**
     * Asserts that the combined stdout and stderr contain the given fragment.
     * Prints both streams in the failure message so the actual output is visible.
     *
     * @param r        the result to inspect
     * @param fragment the substring that must appear
     */
    void assertOutputContains(RunResult r, String fragment) {
        assertTrue((r.stdout() + r.stderr()).contains(fragment),
                "Expected output to contain \"" + fragment + "\"\n"
                        + "stdout: " + r.stdout() + "\nstderr: " + r.stderr());
    }

    /**
     * Asserts that neither stdout nor stderr contains the given fragment.
     * Useful for verifying that a value is absent after a delete or an incorrect default is not applied.
     *
     * @param r        the result to inspect
     * @param fragment the substring that must be absent
     */
    void assertOutputDoesNotContain(RunResult r, String fragment) {
        assertFalse((r.stdout() + r.stderr()).contains(fragment),
                "Expected output NOT to contain \"" + fragment + "\"\n"
                        + "stdout: " + r.stdout() + "\nstderr: " + r.stderr());
    }

    /**
     * Immutable result of one {@code shelter} CLI invocation.
     * Contains exit code, full stdout, and full stderr captured from the subprocess.
     *
     * @param exitCode the process exit code (0 = success)
     * @param stdout   everything written to standard output
     * @param stderr   everything written to standard error
     */
    record RunResult(int exitCode, String stdout, String stderr) {}
}
