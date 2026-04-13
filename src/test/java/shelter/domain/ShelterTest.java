package shelter.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Shelter}.
 * Covers construction validation, capacity enforcement, addAnimal, removeAnimal,
 * containsAnimal, and the unmodifiable list contract.
 */
class ShelterTest {

    private Shelter shelter;
    private Dog dog;
    private Cat cat;

    @BeforeEach
    void setUp() {
        shelter = new Shelter("Happy Paws", "Boston, MA", 2);
        dog = new Dog("Rex", "Labrador", LocalDate.now().minusYears(3), ActivityLevel.HIGH, true, Dog.Size.LARGE, false);
        cat = new Cat("Miso", "Persian", LocalDate.now().minusYears(2), ActivityLevel.LOW, true, true, true);
    }

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    @Test
    void shelter_createsSuccessfully_withValidArguments() {
        assertNotNull(shelter.getId());
        assertEquals("Happy Paws", shelter.getName());
        assertEquals("Boston, MA", shelter.getLocation());
        assertEquals(2, shelter.getCapacity());
        assertEquals(0, shelter.getCurrentCount());
        assertTrue(shelter.hasCapacity());
    }

    @Test
    void shelter_throwsIllegalArgumentException_whenNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Shelter(null, "Boston, MA", 10));
    }

    @Test
    void shelter_throwsIllegalArgumentException_whenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                new Shelter("  ", "Boston, MA", 10));
    }

    @Test
    void shelter_throwsIllegalArgumentException_whenLocationIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Shelter("Happy Paws", null, 10));
    }

    @Test
    void shelter_throwsIllegalArgumentException_whenCapacityIsZero() {
        assertThrows(IllegalArgumentException.class, () ->
                new Shelter("Happy Paws", "Boston, MA", 0));
    }

    @Test
    void shelter_throwsIllegalArgumentException_whenCapacityIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                new Shelter("Happy Paws", "Boston, MA", -5));
    }

    // -------------------------------------------------------------------------
    // addAnimal
    // -------------------------------------------------------------------------

    @Test
    void addAnimal_addsSuccessfully() {
        shelter.addAnimal(dog);
        assertEquals(1, shelter.getCurrentCount());
        assertTrue(shelter.containsAnimal(dog.getId()));
    }

    @Test
    void addAnimal_throwsIllegalArgumentException_whenNull() {
        assertThrows(IllegalArgumentException.class, () -> shelter.addAnimal(null));
    }

    @Test
    void addAnimal_throwsIllegalArgumentException_whenDuplicate() {
        shelter.addAnimal(dog);
        assertThrows(IllegalArgumentException.class, () -> shelter.addAnimal(dog));
    }

    @Test
    void addAnimal_throwsIllegalStateException_whenAtCapacity() {
        shelter.addAnimal(dog);
        shelter.addAnimal(cat);
        Rabbit rabbit = new Rabbit("Bun", "Holland Lop", LocalDate.now().minusYears(1),
                ActivityLevel.MEDIUM, false, Rabbit.FurLength.SHORT);
        assertThrows(IllegalStateException.class, () -> shelter.addAnimal(rabbit));
    }

    // -------------------------------------------------------------------------
    // removeAnimal
    // -------------------------------------------------------------------------

    @Test
    void removeAnimal_removesSuccessfully() {
        shelter.addAnimal(dog);
        shelter.removeAnimal(dog.getId());
        assertFalse(shelter.containsAnimal(dog.getId()));
        assertEquals(0, shelter.getCurrentCount());
    }

    @Test
    void removeAnimal_throwsIllegalArgumentException_whenIdIsNull() {
        assertThrows(IllegalArgumentException.class, () -> shelter.removeAnimal(null));
    }

    @Test
    void removeAnimal_throwsIllegalArgumentException_whenNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                shelter.removeAnimal("nonexistent-id"));
    }

    // -------------------------------------------------------------------------
    // capacity and list contract
    // -------------------------------------------------------------------------

    @Test
    void hasCapacity_returnsFalse_whenFull() {
        shelter.addAnimal(dog);
        shelter.addAnimal(cat);
        assertFalse(shelter.hasCapacity());
    }

    @Test
    void hasCapacity_returnsTrue_afterRemoval() {
        shelter.addAnimal(dog);
        shelter.addAnimal(cat);
        shelter.removeAnimal(dog.getId());
        assertTrue(shelter.hasCapacity());
    }

    @Test
    void getAnimals_returnsUnmodifiableList() {
        shelter.addAnimal(dog);
        List<Animal> animals = shelter.getAnimals();
        assertThrows(UnsupportedOperationException.class, () -> animals.add(cat));
    }

    @Test
    void getAnimals_reflectsCurrentState() {
        assertTrue(shelter.getAnimals().isEmpty());
        shelter.addAnimal(dog);
        assertEquals(1, shelter.getAnimals().size());
        shelter.addAnimal(cat);
        assertEquals(2, shelter.getAnimals().size());
    }
}
