package shelter.startup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CsvRepositoryFactory}.
 * These tests verify that the factory creates the full CSV repository bundle without using the real home directory.
 */
class CsvRepositoryFactoryTest {

    @TempDir
    Path tempDir;

    @Test
    void create_validDataDirectory_returnsCompleteRepositoryBundle() {
        RepositoryBundle repositories = new CsvRepositoryFactory().create(tempDir);

        assertNotNull(repositories.shelterRepository());
        assertNotNull(repositories.animalRepository());
        assertNotNull(repositories.adopterRepository());
        assertNotNull(repositories.adoptionRequestRepository());
        assertNotNull(repositories.transferRequestRepository());
        assertNotNull(repositories.vaccineTypeRepository());
        assertNotNull(repositories.vaccinationRecordRepository());
        assertNotNull(repositories.auditRepository());
    }

    @Test
    void create_validDataDirectory_initializesCsvFiles() {
        new CsvRepositoryFactory().create(tempDir);

        assertTrue(Files.isRegularFile(tempDir.resolve("shelters.csv")));
        assertTrue(Files.isRegularFile(tempDir.resolve("animals.csv")));
        assertTrue(Files.isRegularFile(tempDir.resolve("adopters.csv")));
        assertTrue(Files.isRegularFile(tempDir.resolve("adoption-requests.csv")));
        assertTrue(Files.isRegularFile(tempDir.resolve("transfer-requests.csv")));
        assertTrue(Files.isRegularFile(tempDir.resolve("vaccine-types.csv")));
        assertTrue(Files.isRegularFile(tempDir.resolve("vaccination-records.csv")));
        assertTrue(Files.isRegularFile(tempDir.resolve("audit.csv")));
    }

    @Test
    void create_nullDataDirectory_throws() {
        CsvRepositoryFactory factory = new CsvRepositoryFactory();

        assertThrows(IllegalArgumentException.class, () -> factory.create(null));
    }
}
