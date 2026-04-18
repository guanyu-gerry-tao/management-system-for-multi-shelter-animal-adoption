package shelter.cli;

import org.junit.jupiter.api.Test;
import shelter.domain.Adopter;
import shelter.domain.AdopterPreferences;
import shelter.domain.DailySchedule;
import shelter.domain.LivingSpace;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AdopterCmd#renderList(PrintWriter, List)}.
 * Verifies comma-headed, space-padded table output and the empty {@code (none)} marker.
 */
class AdopterCmdRenderListTest {

    @Test
    void renderList_withAdopter_hasCommaHeaderAndRow() {
        Adopter a = new Adopter("Alice", LivingSpace.APARTMENT, DailySchedule.HOME_MOST_OF_DAY,
                null, new AdopterPreferences(null, null, null, null, 0, 20));

        StringWriter sw = new StringWriter();
        AdopterCmd.renderList(new PrintWriter(sw), List.of(a));

        String out = sw.toString();
        assertTrue(out.contains("NAME,"));
        assertTrue(out.contains("Alice"));
        assertFalse(out.contains("---"));
    }

    @Test
    void renderList_empty_printsNoneMarker() {
        StringWriter sw = new StringWriter();
        AdopterCmd.renderList(new PrintWriter(sw), List.of());

        assertTrue(sw.toString().contains("(none)"));
    }
}
