package shelter.repository.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import shelter.domain.ActivityLevel;
import shelter.domain.Animal;
import shelter.domain.Cat;
import shelter.domain.Dog;
import shelter.domain.Rabbit;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CsvAnimalRepository}, verifying CRUD operations and query methods
 * for all three concrete animal types using a JUnit {@code @TempDir}.
 */
class CsvAnimalRepositoryTest {

    @TempDir
    Path tempDir;

    private CsvAnimalRepository repo;

    /**
     * Creates a fresh repository backed by the JUnit temp directory before each test.
     * This guarantees tests are isolated and never share persisted state.
     */
    @BeforeEach
    void setUp() {
        repo = new CsvAnimalRepository(tempDir.toString());
    }

    /**
     * Verifies that a saved Dog can be retrieved by ID with all base and species-specific
     * fields intact, including size and neutered status.
     */
    @Test
    void saveAndFindById_dog_returnsCorrectFields() {
        Dog dog = new Dog("Rex", "Labrador", 3, ActivityLevel.HIGH, true, Dog.Size.LARGE, true);
        dog.setShelterId("shelter-1");
        repo.save(dog);

        Optional<Animal> found = repo.findById(dog.getId());
        assertTrue(found.isPresent());
        Dog loaded = (Dog) found.get();
        assertEquals(dog.getId(), loaded.getId());
        assertEquals("Rex", loaded.getName());
        assertEquals("Labrador", loaded.getBreed());
        assertEquals(3, loaded.getAge());
        assertEquals(ActivityLevel.HIGH, loaded.getActivityLevel());
        assertTrue(loaded.isVaccinated());
        assertEquals(Dog.Size.LARGE, loaded.getSize());
        assertTrue(loaded.isNeutered());
        assertEquals("shelter-1", loaded.getShelterId());
    }

    /**
     * Verifies that a saved Cat is round-tripped with its indoor and neutered flags preserved.
     */
    @Test
    void saveAndFindById_cat_returnsCorrectFields() {
        Cat cat = new Cat("Whiskers", "Siamese", 2, ActivityLevel.LOW, false, true, false);
        cat.setShelterId("shelter-2");
        repo.save(cat);

        Cat loaded = (Cat) repo.findById(cat.getId()).orElseThrow();
        assertEquals("Whiskers", loaded.getName());
        assertTrue(loaded.isIndoor());
        assertFalse(loaded.isNeutered());
        assertEquals("shelter-2", loaded.getShelterId());
    }

    /**
     * Verifies that a saved Rabbit is round-tripped with its fur length preserved.
     */
    @Test
    void saveAndFindById_rabbit_returnsCorrectFields() {
        Rabbit rabbit = new Rabbit("Fluffy", "Holland Lop", 1, ActivityLevel.MEDIUM, true, Rabbit.FurLength.LONG);
        rabbit.setShelterId("shelter-3");
        repo.save(rabbit);

        Rabbit loaded = (Rabbit) repo.findById(rabbit.getId()).orElseThrow();
        assertEquals("Fluffy", loaded.getName());
        assertEquals(Rabbit.FurLength.LONG, loaded.getFurLength());
    }

    /**
     * Verifies that findAll returns all animals across all species.
     */
    @Test
    void findAll_returnsAllSavedAnimals() {
        repo.save(new Dog("Dog1", "Breed", 1, ActivityLevel.LOW, false, Dog.Size.SMALL, false));
        repo.save(new Cat("Cat1", "Breed", 2, ActivityLevel.MEDIUM, true, false, true));
        repo.save(new Rabbit("Rabbit1", "Breed", 3, ActivityLevel.HIGH, false, Rabbit.FurLength.SHORT));

        assertEquals(3, repo.findAll().size());
    }

    /**
     * Verifies that delete removes the animal from the store and findById returns empty.
     */
    @Test
    void delete_removesAnimal() {
        Dog dog = new Dog("ToDelete", "Breed", 2, ActivityLevel.MEDIUM, false, Dog.Size.MEDIUM, false);
        repo.save(dog);
        repo.delete(dog.getId());

        assertTrue(repo.findById(dog.getId()).isEmpty());
        assertEquals(0, repo.findAll().size());
    }

    /**
     * Verifies that findById returns empty for an unknown ID.
     */
    @Test
    void findById_unknownId_returnsEmpty() {
        assertTrue(repo.findById("no-such-id").isEmpty());
    }

    /**
     * Verifies that findByShelterId filters animals by their shelter ID.
     */
    @Test
    void findByShelterId_filtersCorrectly() {
        Dog dog1 = new Dog("D1", "Breed", 1, ActivityLevel.LOW, false, Dog.Size.SMALL, false);
        dog1.setShelterId("shelter-A");
        Dog dog2 = new Dog("D2", "Breed", 2, ActivityLevel.LOW, false, Dog.Size.SMALL, false);
        dog2.setShelterId("shelter-B");
        repo.save(dog1);
        repo.save(dog2);

        List<Animal> inA = repo.findByShelterId("shelter-A");
        assertEquals(1, inA.size());
        assertEquals("D1", inA.get(0).getName());
    }

    /**
     * Verifies that findByAdopterId returns only animals adopted by the specified adopter.
     */
    @Test
    void findByAdopterId_filtersCorrectly() {
        Dog dog = new Dog("Buddy", "Breed", 4, ActivityLevel.MEDIUM, true, Dog.Size.MEDIUM, true);
        dog.setShelterId("shelter-X");
        dog.setAdopterId("adopter-99");
        repo.save(dog);

        List<Animal> adopted = repo.findByAdopterId("adopter-99");
        assertEquals(1, adopted.size());
        assertEquals("Buddy", adopted.get(0).getName());
    }

    /**
     * Verifies that an animal with a null adopterId (available) is not returned by findByAdopterId.
     */
    @Test
    void findByAdopterId_availableAnimal_notReturned() {
        Dog dog = new Dog("Available", "Breed", 2, ActivityLevel.LOW, false, Dog.Size.SMALL, false);
        dog.setShelterId("shelter-X");
        repo.save(dog);

        assertTrue(repo.findByAdopterId("some-adopter").isEmpty());
    }

    /**
     * Verifies CSV round-trip: animals saved in one instance are readable in a new instance.
     */
    @Test
    void persistence_roundTrip() {
        Dog dog = new Dog("Persistent", "Beagle", 5, ActivityLevel.MEDIUM, true, Dog.Size.SMALL, false);
        dog.setShelterId("s1");
        repo.save(dog);

        CsvAnimalRepository repo2 = new CsvAnimalRepository(tempDir.toString());
        Optional<Animal> found = repo2.findById(dog.getId());
        assertTrue(found.isPresent());
        assertEquals("Persistent", found.get().getName());
    }
}
