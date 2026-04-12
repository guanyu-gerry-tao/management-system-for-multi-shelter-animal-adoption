package shelter.startup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ApplicationGraph}.
 * These tests verify that repository wiring produces a complete application graph for CLI use.
 */
class ApplicationGraphTest {

    @TempDir
    Path tempDir;

    @Test
    void from_validRepositoryBundle_returnsCompleteApplicationGraph() {
        RepositoryBundle repositories = new CsvRepositoryFactory().create(tempDir);
        ApplicationGraph graph = ApplicationGraph.from(repositories);

        assertNotNull(graph.animalApp());
        assertNotNull(graph.adopterApp());
        assertNotNull(graph.shelterApp());
        assertNotNull(graph.adoptionApp());
        assertNotNull(graph.transferApp());
        assertNotNull(graph.matchingApp());
        assertNotNull(graph.vaccinationApp());
        assertNotNull(graph.auditApp());
        assertNotNull(graph.explanationService());
    }

    @Test
    void from_nullRepositoryBundle_throws() {
        assertThrows(NullPointerException.class, () -> ApplicationGraph.from(null));
    }
}
