package shelter.cli;

import org.junit.jupiter.api.Test;
import shelter.application.model.AnimalView;
import shelter.domain.ActivityLevel;
import shelter.domain.Dog;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AnimalCmd#renderList(PrintWriter, List)}.
 * Verifies comma-headed, space-padded table output and the empty {@code (none)} marker.
 */
class AnimalCmdRenderListTest {

    @Test
    void renderList_withAnimal_hasCommaHeaderAndRow() {
        Dog dog = new Dog("Rex", "Lab", LocalDate.now().minusYears(2), ActivityLevel.LOW,
                false, Dog.Size.MEDIUM, false);
        AnimalView view = new AnimalView(dog, "Happy Tails");

        StringWriter sw = new StringWriter();
        AnimalCmd.renderList(new PrintWriter(sw), List.of(view));

        String out = sw.toString();
        assertTrue(out.contains("NAME,"));
        assertTrue(out.contains("Rex"));
        assertTrue(out.contains("Happy Tails"));
        assertFalse(out.contains("---"));
    }

    @Test
    void renderList_empty_printsNoneMarker() {
        StringWriter sw = new StringWriter();
        AnimalCmd.renderList(new PrintWriter(sw), List.of());

        assertTrue(sw.toString().contains("(none)"));
    }
}
