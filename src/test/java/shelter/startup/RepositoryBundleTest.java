package shelter.startup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RepositoryBundle}.
 * These tests verify that the bundle exposes repositories consistently and rejects invalid construction.
 */
class RepositoryBundleTest {

    @TempDir
    Path tempDir;

    private RepositoryBundle repositories;

    @BeforeEach
    void setUp() {
        repositories = new CsvRepositoryFactory().create(tempDir);
    }

    @Test
    void getters_validBundle_returnRepositories() {
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
    void constructor_nullShelterRepository_throws() {
        assertThrows(NullPointerException.class, () -> new RepositoryBundle(
                null,
                repositories.animalRepository(),
                repositories.adopterRepository(),
                repositories.adoptionRequestRepository(),
                repositories.transferRequestRepository(),
                repositories.vaccineTypeRepository(),
                repositories.vaccinationRecordRepository(),
                repositories.auditRepository()));
    }

    @Test
    void constructor_nullAuditRepository_throws() {
        assertThrows(NullPointerException.class, () -> new RepositoryBundle(
                repositories.shelterRepository(),
                repositories.animalRepository(),
                repositories.adopterRepository(),
                repositories.adoptionRequestRepository(),
                repositories.transferRequestRepository(),
                repositories.vaccineTypeRepository(),
                repositories.vaccinationRecordRepository(),
                null));
    }
}
