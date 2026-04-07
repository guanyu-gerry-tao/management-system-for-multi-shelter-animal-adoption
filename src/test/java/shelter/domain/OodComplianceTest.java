package shelter.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests OOD compliance methods (equals, hashCode, compareTo, toString, copy constructor)
 * for all domain classes.
 */
class OodComplianceTest {

    private Dog dog;
    private Cat cat;
    private Rabbit rabbit;
    private Shelter shelter;
    private Adopter adopter;
    private AdopterPreferences prefs;
    private Staff staff;
    private VaccineType vaccineType;

    @BeforeEach
    void setUp() {
        prefs = new AdopterPreferences("Dog", "Labrador", ActivityLevel.MEDIUM, 1, 10);
        dog = new Dog("Max", "Labrador", 3, ActivityLevel.MEDIUM, true, Dog.Size.LARGE, true);
        cat = new Cat("Whiskers", "Siamese", 2, ActivityLevel.LOW, false, true, false);
        rabbit = new Rabbit("Floppy", "Holland Lop", 1, ActivityLevel.LOW, true, Rabbit.FurLength.SHORT);
        shelter = new Shelter("Happy Paws", "Boston", 20);
        adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD, DailySchedule.HOME_MOST_OF_DAY, "Loves dogs", prefs);
        staff = new Staff("Admin", "Coordinator");
        vaccineType = new VaccineType("Rabies", "Dog", 365);
    }

    // ===== Animal equals/hashCode =====

    @Test
    void animalEqualsReflexive() {
        assertEquals(dog, dog);
    }

    @Test
    void animalEqualsSymmetric() {
        Dog copy = new Dog(dog);
        assertEquals(dog, copy);
        assertEquals(copy, dog);
    }

    @Test
    void animalNotEqualToDifferentAnimal() {
        Dog other = new Dog("Buddy", "Poodle", 2, ActivityLevel.HIGH, false, Dog.Size.SMALL, false);
        assertNotEquals(dog, other);
    }

    @Test
    void animalNotEqualToNull() {
        assertNotEquals(null, dog);
    }

    @Test
    void animalNotEqualToDifferentType() {
        assertNotEquals(dog, "not an animal");
    }

    @Test
    void animalHashCodeConsistent() {
        Dog copy = new Dog(dog);
        assertEquals(dog.hashCode(), copy.hashCode());
    }

    // ===== Animal compareTo =====

    @Test
    void animalCompareToByName() {
        List<Animal> animals = new ArrayList<>();
        animals.add(dog);       // Max
        animals.add(cat);       // Whiskers
        animals.add(rabbit);    // Floppy
        Collections.sort(animals);
        assertEquals("Floppy", animals.get(0).getName());
        assertEquals("Max", animals.get(1).getName());
        assertEquals("Whiskers", animals.get(2).getName());
    }

    // ===== Dog copy constructor & toString =====

    @Test
    void dogCopyConstructor() {
        Dog copy = new Dog(dog);
        assertEquals(dog, copy);
        assertEquals(dog.getId(), copy.getId());
        assertEquals(dog.getName(), copy.getName());
        assertEquals(dog.getSize(), copy.getSize());
        assertEquals(dog.isNeutered(), copy.isNeutered());
    }

    @Test
    void dogToStringContainsInfo() {
        String s = dog.toString();
        assertTrue(s.contains("Max"));
        assertTrue(s.contains("size="));
        assertTrue(s.contains("neutered="));
    }

    // ===== Cat copy constructor & toString =====

    @Test
    void catCopyConstructor() {
        Cat copy = new Cat(cat);
        assertEquals(cat, copy);
        assertEquals(cat.getId(), copy.getId());
        assertEquals(cat.isIndoor(), copy.isIndoor());
    }

    @Test
    void catToStringContainsInfo() {
        String s = cat.toString();
        assertTrue(s.contains("Whiskers"));
        assertTrue(s.contains("indoor="));
    }

    // ===== Rabbit copy constructor & toString =====

    @Test
    void rabbitCopyConstructor() {
        Rabbit copy = new Rabbit(rabbit);
        assertEquals(rabbit, copy);
        assertEquals(rabbit.getFurLength(), copy.getFurLength());
    }

    @Test
    void rabbitToStringContainsInfo() {
        String s = rabbit.toString();
        assertTrue(s.contains("Floppy"));
        assertTrue(s.contains("furLength="));
    }

    // ===== Shelter =====

    @Test
    void shelterEqualsById() {
        Shelter copy = new Shelter(shelter);
        assertEquals(shelter, copy);
        Shelter other = new Shelter("Happy Paws", "Boston", 20);
        assertNotEquals(shelter, other);
    }

    @Test
    void shelterHashCodeConsistent() {
        Shelter copy = new Shelter(shelter);
        assertEquals(shelter.hashCode(), copy.hashCode());
    }

    @Test
    void shelterCompareToByName() {
        Shelter a = new Shelter("Alpha", "NYC", 10);
        Shelter b = new Shelter("Beta", "LA", 10);
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
    }

    @Test
    void shelterCopyConstructorCopiesAnimals() {
        shelter.addAnimal(dog);
        Shelter copy = new Shelter(shelter);
        assertEquals(1, copy.getCurrentCount());
        assertEquals(shelter.getId(), copy.getId());
    }

    @Test
    void shelterToStringContainsInfo() {
        String s = shelter.toString();
        assertTrue(s.contains("Happy Paws"));
        assertTrue(s.contains("Boston"));
    }

    // ===== Adopter =====

    @Test
    void adopterEqualsById() {
        Adopter copy = new Adopter(adopter);
        assertEquals(adopter, copy);
    }

    @Test
    void adopterHashCodeConsistent() {
        Adopter copy = new Adopter(adopter);
        assertEquals(adopter.hashCode(), copy.hashCode());
    }

    @Test
    void adopterCompareToByName() {
        Adopter bob = new Adopter("Bob", LivingSpace.APARTMENT, DailySchedule.AWAY_PART_OF_DAY, null, prefs);
        assertTrue(adopter.compareTo(bob) < 0); // Alice < Bob
    }

    @Test
    void adopterCopyConstructor() {
        adopter.addAdoptedAnimalId("animal-1");
        Adopter copy = new Adopter(adopter);
        assertEquals(adopter.getId(), copy.getId());
        assertEquals(adopter.getName(), copy.getName());
        assertEquals(1, copy.getAdoptedAnimalIds().size());
    }

    @Test
    void adopterToStringContainsInfo() {
        String s = adopter.toString();
        assertTrue(s.contains("Alice"));
        assertTrue(s.contains("HOUSE_WITH_YARD"));
    }

    // ===== Staff =====

    @Test
    void staffCopyConstructor() {
        Staff copy = new Staff(staff);
        assertEquals(staff, copy);
        assertEquals(staff.getId(), copy.getId());
        assertEquals(staff.getName(), copy.getName());
        assertEquals(staff.getRole(), copy.getRole());
    }

    @Test
    void staffCompareToByName() {
        Staff alice = new Staff("Alice", "Vet");
        Staff bob = new Staff("Bob", "Admin");
        assertTrue(alice.compareTo(bob) < 0);
    }

    // ===== VaccineType =====

    @Test
    void vaccineTypeEqualsById() {
        VaccineType copy = new VaccineType(vaccineType);
        assertEquals(vaccineType, copy);
        VaccineType other = new VaccineType("Rabies", "Dog", 365);
        assertNotEquals(vaccineType, other);
    }

    @Test
    void vaccineTypeHashCodeConsistent() {
        VaccineType copy = new VaccineType(vaccineType);
        assertEquals(vaccineType.hashCode(), copy.hashCode());
    }

    @Test
    void vaccineTypeCompareToByName() {
        VaccineType a = new VaccineType("DHPP", "Dog", 365);
        VaccineType b = new VaccineType("Rabies", "Dog", 365);
        assertTrue(a.compareTo(b) < 0);
    }

    @Test
    void vaccineTypeCopyConstructor() {
        VaccineType copy = new VaccineType(vaccineType);
        assertEquals(vaccineType.getId(), copy.getId());
        assertEquals(vaccineType.getName(), copy.getName());
        assertEquals(vaccineType.getApplicableSpecies(), copy.getApplicableSpecies());
        assertEquals(vaccineType.getValidityDays(), copy.getValidityDays());
    }

    // ===== AdoptionRequest =====

    @Test
    void adoptionRequestEqualsById() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        AdoptionRequest copy = new AdoptionRequest(req);
        assertEquals(req, copy);
    }

    @Test
    void adoptionRequestHashCodeConsistent() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        AdoptionRequest copy = new AdoptionRequest(req);
        assertEquals(req.hashCode(), copy.hashCode());
    }

    @Test
    void adoptionRequestCompareToBySubmittedAt() throws InterruptedException {
        AdoptionRequest first = new AdoptionRequest(adopter, dog);
        Thread.sleep(10);
        AdoptionRequest second = new AdoptionRequest(adopter, cat);
        assertTrue(first.compareTo(second) < 0);
    }

    @Test
    void adoptionRequestCopyConstructor() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        AdoptionRequest copy = new AdoptionRequest(req);
        assertEquals(req.getId(), copy.getId());
        assertEquals(req.getStatus(), copy.getStatus());
        assertEquals(req.getSubmittedAt(), copy.getSubmittedAt());
    }

    @Test
    void adoptionRequestToStringContainsInfo() {
        AdoptionRequest req = new AdoptionRequest(adopter, dog);
        String s = req.toString();
        assertTrue(s.contains("Alice"));
        assertTrue(s.contains("Max"));
        assertTrue(s.contains("PENDING"));
    }

    // ===== TransferRequest =====

    @Test
    void transferRequestEqualsById() {
        Shelter dest = new Shelter("Other Shelter", "NYC", 10);
        shelter.addAnimal(dog);
        TransferRequest req = new TransferRequest(dog, shelter, dest);
        TransferRequest copy = new TransferRequest(req);
        assertEquals(req, copy);
    }

    @Test
    void transferRequestCompareToByRequestedAt() throws InterruptedException {
        Shelter dest = new Shelter("Other Shelter", "NYC", 10);
        shelter.addAnimal(dog);
        shelter.addAnimal(cat);
        TransferRequest first = new TransferRequest(dog, shelter, dest);
        Thread.sleep(10);
        TransferRequest second = new TransferRequest(cat, shelter, dest);
        assertTrue(first.compareTo(second) < 0);
    }

    @Test
    void transferRequestToStringContainsInfo() {
        Shelter dest = new Shelter("Other Shelter", "NYC", 10);
        shelter.addAnimal(dog);
        TransferRequest req = new TransferRequest(dog, shelter, dest);
        String s = req.toString();
        assertTrue(s.contains("Max"));
        assertTrue(s.contains("Happy Paws"));
        assertTrue(s.contains("Other Shelter"));
    }

    // ===== VaccinationRecord =====

    @Test
    void vaccinationRecordEqualsById() {
        VaccinationRecord rec = new VaccinationRecord("a1", "v1", LocalDate.of(2025, 1, 1));
        VaccinationRecord copy = new VaccinationRecord(rec);
        assertEquals(rec, copy);
    }

    @Test
    void vaccinationRecordHashCodeConsistent() {
        VaccinationRecord rec = new VaccinationRecord("a1", "v1", LocalDate.of(2025, 1, 1));
        VaccinationRecord copy = new VaccinationRecord(rec);
        assertEquals(rec.hashCode(), copy.hashCode());
    }

    @Test
    void vaccinationRecordCompareToByDate() {
        VaccinationRecord earlier = new VaccinationRecord("a1", "v1", LocalDate.of(2025, 1, 1));
        VaccinationRecord later = new VaccinationRecord("a1", "v1", LocalDate.of(2025, 6, 1));
        assertTrue(earlier.compareTo(later) < 0);
    }

    @Test
    void vaccinationRecordDifferentIdsNotEqual() {
        VaccinationRecord r1 = new VaccinationRecord("a1", "v1", LocalDate.of(2025, 1, 1));
        VaccinationRecord r2 = new VaccinationRecord("a1", "v1", LocalDate.of(2025, 1, 1));
        assertNotEquals(r1, r2); // different UUIDs
    }

    // ===== AdopterPreferences =====

    @Test
    void adopterPreferencesEqualsByAllFields() {
        AdopterPreferences p1 = new AdopterPreferences("Dog", "Lab", ActivityLevel.HIGH, 1, 5);
        AdopterPreferences p2 = new AdopterPreferences("Dog", "Lab", ActivityLevel.HIGH, 1, 5);
        assertEquals(p1, p2);
    }

    @Test
    void adopterPreferencesNotEqualWhenFieldsDiffer() {
        AdopterPreferences p1 = new AdopterPreferences("Dog", "Lab", ActivityLevel.HIGH, 1, 5);
        AdopterPreferences p2 = new AdopterPreferences("Cat", "Lab", ActivityLevel.HIGH, 1, 5);
        assertNotEquals(p1, p2);
    }

    @Test
    void adopterPreferencesHashCodeConsistent() {
        AdopterPreferences p1 = new AdopterPreferences("Dog", "Lab", ActivityLevel.HIGH, 1, 5);
        AdopterPreferences p2 = new AdopterPreferences("Dog", "Lab", ActivityLevel.HIGH, 1, 5);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void adopterPreferencesCopyConstructor() {
        AdopterPreferences copy = new AdopterPreferences(prefs);
        assertEquals(prefs, copy);
        assertEquals(prefs.getPreferredSpecies(), copy.getPreferredSpecies());
        assertEquals(prefs.getMinAge(), copy.getMinAge());
        assertEquals(prefs.getMaxAge(), copy.getMaxAge());
    }

    @Test
    void adopterPreferencesToStringContainsInfo() {
        String s = prefs.toString();
        assertTrue(s.contains("Dog"));
        assertTrue(s.contains("Labrador"));
        assertTrue(s.contains("MEDIUM"));
    }

    @Test
    void adopterPreferencesWithNullFields() {
        AdopterPreferences p1 = new AdopterPreferences(null, null, null, 0, 20);
        AdopterPreferences p2 = new AdopterPreferences(null, null, null, 0, 20);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }
}
