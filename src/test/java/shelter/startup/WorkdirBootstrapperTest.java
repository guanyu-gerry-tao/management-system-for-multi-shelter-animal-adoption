package shelter.startup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WorkdirBootstrapper}.
 * The tests use a temporary directory so startup file creation never touches the real user home.
 */
class WorkdirBootstrapperTest {

    @TempDir
    Path tempDir;

    @Test
    void bootstrap_missingWorkdir_createsDataDirectoryAndClaudeFile() throws IOException {
        Path shelterHome = tempDir.resolve("shelter");
        WorkdirBootstrapper bootstrapper = new WorkdirBootstrapper();

        bootstrapper.bootstrap(shelterHome);

        assertTrue(Files.isDirectory(shelterHome));
        assertTrue(Files.isDirectory(shelterHome.resolve("data")));
        assertTrue(Files.isRegularFile(shelterHome.resolve("CLAUDE.md")));
        assertTrue(Files.readString(shelterHome.resolve("CLAUDE.md"))
                .contains("Command Reference"));
    }

    @Test
    void bootstrap_existingClaudeFile_doesNotOverwrite() throws IOException {
        Path shelterHome = tempDir.resolve("shelter");
        Path claudeFile = shelterHome.resolve("CLAUDE.md");
        Files.createDirectories(shelterHome);
        Files.writeString(claudeFile, "custom user content");

        new WorkdirBootstrapper().bootstrap(shelterHome);

        assertEquals("custom user content", Files.readString(claudeFile));
        assertTrue(Files.isDirectory(shelterHome.resolve("data")));
    }

    @Test
    void bootstrap_nullShelterHome_throws() {
        WorkdirBootstrapper bootstrapper = new WorkdirBootstrapper();

        assertThrows(NullPointerException.class, () -> bootstrapper.bootstrap(null));
    }
}
