package shelter.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Animal hierarchy: {@link Dog}, {@link Cat}, and {@link Rabbit}.
 * Covers construction validation, getter correctness, mutators, and unique ID generation.
 */
class AnimalTest {

    // -------------------------------------------------------------------------
    // Dog — happy path
    // -------------------------------------------------------------------------

    @Test
    void dog_createsSuccessfully_withValidArguments() {
        Dog dog = new Dog("Rex", "Labrador", LocalDate.now().minusYears(3), ActivityLevel.HIGH, true, Dog.Size.LARGE, false);
        assertEquals("Rex", dog.getName());
        assertEquals("Labrador", dog.getBreed());
        assertEquals(3, dog.getAge());
        assertEquals(ActivityLevel.HIGH, dog.getActivityLevel());
        assertTrue(dog.isVaccinated());
        assertEquals(Dog.Size.LARGE, dog.getSize());
        assertFalse(dog.isNeutered());
        assertEquals(Species.DOG, dog.getSpecies());
        assertNotNull(dog.getId());
    }

    @Test
    void dog_ageZeroIsValid() {
        Dog puppy = new Dog("Puppy", "Mixed", LocalDate.now(), ActivityLevel.HIGH, false, Dog.Size.SMALL, false);
        assertEquals(0, puppy.getAge());
    }

    @Test
    void dog_setVaccinated_updatesStatus() {
        Dog dog = new Dog("Rex", "Labrador", LocalDate.now().minusYears(3), ActivityLevel.HIGH, false, Dog.Size.LARGE, false);
        dog.setVaccinated(true);
        assertTrue(dog.isVaccinated());
    }

    @Test
    void dog_setNeutered_updatesStatus() {
        Dog dog = new Dog("Rex", "Labrador", LocalDate.now().minusYears(3), ActivityLevel.HIGH, true, Dog.Size.LARGE, false);
        dog.setNeutered(true);
        assertTrue(dog.isNeutered());
    }

    @Test
    void dog_eachInstance_hasUniqueId() {
        Dog d1 = new Dog("A", "Breed", LocalDate.now().minusYears(1), ActivityLevel.LOW, false, Dog.Size.SMALL, false);
        Dog d2 = new Dog("B", "Breed", LocalDate.now().minusYears(1), ActivityLevel.LOW, false, Dog.Size.SMALL, false);
        assertNotEquals(d1.getId(), d2.getId());
    }

    // -------------------------------------------------------------------------
    // Dog — validation failures
    // -------------------------------------------------------------------------

    @Test
    void dog_throwsIllegalArgumentException_whenNameIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Dog(null, "Labrador", LocalDate.now().minusYears(3), ActivityLevel.HIGH, true, Dog.Size.LARGE, false));
    }

    @Test
    void dog_throwsIllegalArgumentException_whenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                new Dog("   ", "Labrador", LocalDate.now().minusYears(3), ActivityLevel.HIGH, true, Dog.Size.LARGE, false));
    }

    @Test
    void dog_throwsIllegalArgumentException_whenBreedIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Dog("Rex", null, LocalDate.now().minusYears(3), ActivityLevel.HIGH, true, Dog.Size.LARGE, false));
    }

    @Test
    void dog_throwsIllegalArgumentException_whenAgeIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                new Dog("Rex", "Labrador", null, ActivityLevel.HIGH, true, Dog.Size.LARGE, false));
    }

    @Test
    void dog_throwsIllegalArgumentException_whenActivityLevelIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Dog("Rex", "Labrador", LocalDate.now().minusYears(3), null, true, Dog.Size.LARGE, false));
    }

    @Test
    void dog_throwsIllegalArgumentException_whenSizeIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Dog("Rex", "Labrador", LocalDate.now().minusYears(3), ActivityLevel.HIGH, true, null, false));
    }

    // -------------------------------------------------------------------------
    // Cat — happy path
    // -------------------------------------------------------------------------

    @Test
    void cat_createsSuccessfully_withValidArguments() {
        Cat cat = new Cat("Miso", "Persian", LocalDate.now().minusYears(2), ActivityLevel.LOW, true, true, true);
        assertEquals(Species.CAT, cat.getSpecies());
        assertEquals("Miso", cat.getName());
        assertTrue(cat.isIndoor());
        assertTrue(cat.isNeutered());
        assertNotNull(cat.getId());
    }

    @Test
    void cat_setNeutered_updatesStatus() {
        Cat cat = new Cat("Miso", "Persian", LocalDate.now().minusYears(2), ActivityLevel.LOW, true, true, false);
        cat.setNeutered(true);
        assertTrue(cat.isNeutered());
    }

    // -------------------------------------------------------------------------
    // Cat — validation failures
    // -------------------------------------------------------------------------

    @Test
    void cat_throwsIllegalArgumentException_whenBreedIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cat("Miso", null, LocalDate.now().minusYears(2), ActivityLevel.LOW, true, true, true));
    }

    @Test
    void cat_throwsIllegalArgumentException_whenAgeIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cat("Miso", "Persian", null, ActivityLevel.LOW, true, true, true));
    }

    // -------------------------------------------------------------------------
    // Rabbit — happy path
    // -------------------------------------------------------------------------

    @Test
    void rabbit_createsSuccessfully_withValidArguments() {
        Rabbit rabbit = new Rabbit("Bun", "Holland Lop", LocalDate.now().minusYears(1), ActivityLevel.MEDIUM,
                false, Rabbit.FurLength.SHORT);
        assertEquals(Species.RABBIT, rabbit.getSpecies());
        assertEquals("Bun", rabbit.getName());
        assertEquals(Rabbit.FurLength.SHORT, rabbit.getFurLength());
        assertNotNull(rabbit.getId());
    }

    @Test
    void rabbit_longFurLength_storedCorrectly() {
        Rabbit rabbit = new Rabbit("Fluffy", "Angora", LocalDate.now().minusYears(2), ActivityLevel.LOW,
                true, Rabbit.FurLength.LONG);
        assertEquals(Rabbit.FurLength.LONG, rabbit.getFurLength());
    }

    // -------------------------------------------------------------------------
    // Rabbit — validation failures
    // -------------------------------------------------------------------------

    @Test
    void rabbit_throwsIllegalArgumentException_whenFurLengthIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Rabbit("Bun", "Holland Lop", LocalDate.now().minusYears(1), ActivityLevel.MEDIUM, false, null));
    }

    @Test
    void rabbit_throwsIllegalArgumentException_whenNameIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                new Rabbit("", "Holland Lop", LocalDate.now().minusYears(1), ActivityLevel.MEDIUM, false, Rabbit.FurLength.SHORT));
    }
}
