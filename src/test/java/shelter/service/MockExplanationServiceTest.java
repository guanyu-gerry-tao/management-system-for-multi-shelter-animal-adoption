package shelter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shelter.domain.*;
import shelter.service.model.ExplanationResult;
import shelter.service.model.MatchResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MockExplanationService} to verify deterministic behavior
 * and compliance with the {@link ExplanationService} interface.
 */
class MockExplanationServiceTest {

    private MockExplanationService service;
    private Adopter adopter;
    private Dog dog;

    @BeforeEach
    void setUp() {
        service = new MockExplanationService();
        AdopterPreferences prefs = new AdopterPreferences("Dog", "Lab", ActivityLevel.MEDIUM, 1, 10);
        adopter = new Adopter("Alice", LivingSpace.HOUSE_WITH_YARD, DailySchedule.HOME_MOST_OF_DAY, null, prefs);
        dog = new Dog("Max", "Labrador", 3, ActivityLevel.MEDIUM, true, Dog.Size.LARGE, true);
    }

    @Test
    void explainWithResults() {
        List<MatchResult> results = List.of(new MatchResult(dog, adopter, 85));
        ExplanationResult result = service.explain(results);

        assertNotNull(result);
        assertTrue(result.getMatchRationale().contains("[Mock]"));
        assertTrue(result.getMatchRationale().contains("Max"));
        assertTrue(result.getMatchRationale().contains("Alice"));
        assertTrue(result.getConfidenceAssessment().contains("[Mock]"));
        assertTrue(result.getPersonalizedAdvice().contains("[Mock]"));
    }

    @Test
    void explainWithEmptyList() {
        ExplanationResult result = service.explain(new ArrayList<>());

        assertNotNull(result);
        assertTrue(result.getMatchRationale().contains("[Mock]"));
        assertTrue(result.getMatchRationale().contains("No matches"));
    }

    @Test
    void explainWithNullList() {
        ExplanationResult result = service.explain(null);

        assertNotNull(result);
        assertTrue(result.getMatchRationale().contains("No matches"));
    }

    @Test
    void explainIsDeterministic() {
        List<MatchResult> results = List.of(new MatchResult(dog, adopter, 85));
        ExplanationResult first = service.explain(results);
        ExplanationResult second = service.explain(results);
        assertEquals(first, second);
    }

    @Test
    void explainImplementsInterface() {
        ExplanationService asInterface = service;
        ExplanationResult result = asInterface.explain(List.of(new MatchResult(dog, adopter, 50)));
        assertNotNull(result);
    }
}
