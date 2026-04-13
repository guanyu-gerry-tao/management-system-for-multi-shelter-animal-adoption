package shelter.startup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SystemStartupImpl}.
 * These tests verify that startup can initialize a complete application graph from an isolated temp directory.
 */
class SystemStartupImplTest {

    @TempDir
    Path tempDir;

    @Test
    void initialize_tempDirectory_createsWorkdirAndWiresApplicationGraph() {
        Path shelterHome = tempDir.resolve("shelter");
        SystemStartupImpl startup = new SystemStartupImpl(shelterHome);

        startup.initialize();

        assertTrue(Files.isDirectory(shelterHome));
        assertTrue(Files.isDirectory(shelterHome.resolve("data")));
        assertTrue(Files.isRegularFile(shelterHome.resolve("CLAUDE.md")));
        assertNotNull(startup.animalApp());
        assertNotNull(startup.adopterApp());
        assertNotNull(startup.shelterApp());
        assertNotNull(startup.adoptionApp());
        assertNotNull(startup.transferApp());
        assertNotNull(startup.matchingApp());
        assertNotNull(startup.vaccinationApp());
        assertNotNull(startup.auditApp());
        assertNotNull(startup.explanationService());
    }

    @Test
    void initialize_calledTwice_keepsSameWiredGraph() {
        SystemStartupImpl startup = new SystemStartupImpl(tempDir.resolve("shelter"));

        startup.initialize();
        Object animalApp = startup.animalApp();
        startup.initialize();

        assertSame(animalApp, startup.animalApp());
    }

    @Test
    void constructor_nullShelterHome_throws() {
        assertThrows(NullPointerException.class, () -> new SystemStartupImpl(null));
    }
}
