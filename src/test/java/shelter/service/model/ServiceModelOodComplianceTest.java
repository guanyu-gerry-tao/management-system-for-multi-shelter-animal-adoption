package shelter.service.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests OOD compliance methods (equals, hashCode, compareTo, copy constructor)
 * for all service model classes.
 */
class ServiceModelOodComplianceTest {

    private Adopter adopter;
    private Dog dog;
    private Staff staff;
    private VaccineType vaccineType;

    @BeforeEach
    void setUp() {
        AdopterPreferences prefs = new AdopterPreferences("Dog", "Lab", ActivityLevel.MEDIUM, 1, 10);
        adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD, DailySchedule.HOME_MOST_OF_DAY, null, prefs);
        dog = new Dog("Max", "Labrador", 3, ActivityLevel.MEDIUM, true, Dog.Size.LARGE, true);
        staff = new Staff("Admin", "Coordinator");
        vaccineType = new VaccineType("Rabies", "Dog", 365);
    }

    // ===== MatchResult =====

    @Test
    void matchResultCopyConstructor() {
        MatchResult original = new MatchResult(dog, adopter, 85);
        MatchResult copy = new MatchResult(original);
        assertEquals(original, copy);
        assertEquals(original.getScore(), copy.getScore());
    }

    // ===== AuditEntry =====

    @Test
    void auditEntryEqualsByAllFields() {
        LocalDateTime ts = LocalDateTime.of(2025, 6, 1, 10, 0);
        AuditEntry<String> a1 = new AuditEntry<>(staff, "CREATE", "target1", ts);
        AuditEntry<String> a2 = new AuditEntry<>(staff, "CREATE", "target1", ts);
        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    void auditEntryNotEqualWhenFieldsDiffer() {
        LocalDateTime ts = LocalDateTime.of(2025, 6, 1, 10, 0);
        AuditEntry<String> a1 = new AuditEntry<>(staff, "CREATE", "target1", ts);
        AuditEntry<String> a2 = new AuditEntry<>(staff, "DELETE", "target1", ts);
        assertNotEquals(a1, a2);
    }

    @Test
    void auditEntryCompareToByTimestamp() {
        LocalDateTime earlier = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime later = LocalDateTime.of(2025, 6, 1, 10, 0);
        AuditEntry<String> first = new AuditEntry<>(staff, "CREATE", "t1", earlier);
        AuditEntry<String> second = new AuditEntry<>(staff, "CREATE", "t1", later);
        assertTrue(first.compareTo(second) < 0);
    }

    @Test
    void auditEntrySortByTimestamp() {
        LocalDateTime t1 = LocalDateTime.of(2025, 3, 1, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime t3 = LocalDateTime.of(2025, 2, 1, 10, 0);
        List<AuditEntry<String>> entries = new ArrayList<>();
        entries.add(new AuditEntry<>(staff, "A", "t", t1));
        entries.add(new AuditEntry<>(staff, "B", "t", t2));
        entries.add(new AuditEntry<>(staff, "C", "t", t3));
        Collections.sort(entries);
        assertEquals("B", entries.get(0).getAction());
        assertEquals("C", entries.get(1).getAction());
        assertEquals("A", entries.get(2).getAction());
    }

    @Test
    void auditEntryCopyConstructor() {
        LocalDateTime ts = LocalDateTime.of(2025, 6, 1, 10, 0);
        AuditEntry<String> original = new AuditEntry<>(staff, "CREATE", "target1", ts);
        AuditEntry<String> copy = new AuditEntry<>(original);
        assertEquals(original, copy);
    }

    // ===== ExplanationResult =====

    @Test
    void explanationResultEqualsByAllFields() {
        ExplanationResult r1 = new ExplanationResult("rationale", "confidence", "advice");
        ExplanationResult r2 = new ExplanationResult("rationale", "confidence", "advice");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void explanationResultNotEqualWhenFieldsDiffer() {
        ExplanationResult r1 = new ExplanationResult("rationale", "confidence", "advice");
        ExplanationResult r2 = new ExplanationResult("different", "confidence", "advice");
        assertNotEquals(r1, r2);
    }

    @Test
    void explanationResultWithNulls() {
        ExplanationResult r1 = new ExplanationResult(null, null, null);
        ExplanationResult r2 = new ExplanationResult(null, null, null);
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void explanationResultCopyConstructor() {
        ExplanationResult original = new ExplanationResult("r", "c", "a");
        ExplanationResult copy = new ExplanationResult(original);
        assertEquals(original, copy);
    }

    // ===== NotificationRecord =====

    @Test
    void notificationRecordEqualsByAllFields() {
        LocalDateTime ts = LocalDateTime.of(2025, 6, 1, 10, 0);
        NotificationRecord n1 = new NotificationRecord(staff, "APPROVE", "req-1", ts);
        NotificationRecord n2 = new NotificationRecord(staff, "APPROVE", "req-1", ts);
        assertEquals(n1, n2);
        assertEquals(n1.hashCode(), n2.hashCode());
    }

    @Test
    void notificationRecordNotEqualWhenFieldsDiffer() {
        LocalDateTime ts = LocalDateTime.of(2025, 6, 1, 10, 0);
        NotificationRecord n1 = new NotificationRecord(staff, "APPROVE", "req-1", ts);
        NotificationRecord n2 = new NotificationRecord(staff, "REJECT", "req-1", ts);
        assertNotEquals(n1, n2);
    }

    @Test
    void notificationRecordCompareToByTimestamp() {
        LocalDateTime earlier = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime later = LocalDateTime.of(2025, 6, 1, 10, 0);
        NotificationRecord first = new NotificationRecord(staff, "A", "t1", earlier);
        NotificationRecord second = new NotificationRecord(staff, "A", "t1", later);
        assertTrue(first.compareTo(second) < 0);
    }

    @Test
    void notificationRecordCopyConstructor() {
        LocalDateTime ts = LocalDateTime.of(2025, 6, 1, 10, 0);
        NotificationRecord original = new NotificationRecord(staff, "APPROVE", "req-1", ts);
        NotificationRecord copy = new NotificationRecord(original);
        assertEquals(original, copy);
    }

    // ===== OverdueVaccination =====

    @Test
    void overdueVaccinationCopyConstructor() {
        OverdueVaccination original = new OverdueVaccination(
                vaccineType, LocalDate.of(2024, 6, 1), LocalDate.of(2025, 6, 1));
        OverdueVaccination copy = new OverdueVaccination(original);
        assertEquals(original, copy);
        assertEquals(original.getVaccineType(), copy.getVaccineType());
        assertEquals(original.getLastAdministered(), copy.getLastAdministered());
        assertEquals(original.getDueDate(), copy.getDueDate());
    }

    @Test
    void overdueVaccinationCopyConstructorWithNullLastAdministered() {
        OverdueVaccination original = new OverdueVaccination(
                vaccineType, null, LocalDate.of(2025, 6, 1));
        OverdueVaccination copy = new OverdueVaccination(original);
        assertEquals(original, copy);
        assertNull(copy.getLastAdministered());
    }
}
