package shelter.startup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import shelter.domain.ActivityLevel;
import shelter.domain.Dog;
import shelter.domain.Shelter;

import java.time.LocalDate;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ShelterAnimalLinker}.
 * The linker is tested with real CSV repositories so the test matches the startup reload flow.
 */
class ShelterAnimalLinkerTest {

    @TempDir
    Path tempDir;

    @Test
    void restoreLinks_animalWithShelterId_addsAnimalToShelter() {
        RepositoryBundle repositories = new CsvRepositoryFactory().create(tempDir);
        Shelter shelter = new Shelter("Happy Paws", "Boston", 10);
        Dog dog = new Dog("Rex", "Labrador", LocalDate.now().minusYears(3),
                ActivityLevel.MEDIUM, false, Dog.Size.LARGE, false);
        dog.setShelterId(shelter.getId());
        repositories.shelterRepository().save(shelter);
        repositories.animalRepository().save(dog);

        new ShelterAnimalLinker().restoreLinks(repositories);

        Shelter reloadedShelter = repositories.shelterRepository().findById(shelter.getId()).orElseThrow();
        assertEquals(1, reloadedShelter.getAnimals().size());
        assertEquals(dog.getId(), reloadedShelter.getAnimals().get(0).getId());
    }

    @Test
    void restoreLinks_missingShelter_doesNotThrow() {
        RepositoryBundle repositories = new CsvRepositoryFactory().create(tempDir);
        Dog dog = new Dog("Rex", "Labrador", LocalDate.now().minusYears(3),
                ActivityLevel.MEDIUM, false, Dog.Size.LARGE, false);
        dog.setShelterId("missing-shelter");
        repositories.animalRepository().save(dog);

        assertDoesNotThrow(() -> new ShelterAnimalLinker().restoreLinks(repositories));
    }

    @Test
    void restoreLinks_nullRepositoryBundle_throws() {
        ShelterAnimalLinker linker = new ShelterAnimalLinker();

        assertThrows(NullPointerException.class, () -> linker.restoreLinks(null));
    }
}
