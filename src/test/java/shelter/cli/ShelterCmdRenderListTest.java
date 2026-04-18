package shelter.cli;

import org.junit.jupiter.api.Test;
import shelter.domain.Shelter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ShelterCmd#renderList(PrintWriter, List)}.
 * Verifies comma-headed, space-padded table output and the empty {@code (none)} marker.
 */
class ShelterCmdRenderListTest {

    @Test
    void renderList_withShelters_hasCommaHeaderAndPaddedRows() {
        Shelter a = new Shelter("Alpha", "Boston", 10);

        StringWriter sw = new StringWriter();
        ShelterCmd.renderList(new PrintWriter(sw), List.of(a));

        String out = sw.toString();
        assertTrue(out.contains("NAME,"), "header has comma after NAME");
        assertTrue(out.contains("LOCATION,"), "header has comma after LOCATION");
        assertTrue(out.contains("Alpha"), "row has data");
        assertTrue(out.contains("Boston"));
        assertFalse(out.contains("---"), "no dashes in output");
        assertFalse(out.contains("|"), "no pipes in output");
    }

    @Test
    void renderList_empty_printsNoneMarker() {
        StringWriter sw = new StringWriter();
        ShelterCmd.renderList(new PrintWriter(sw), List.of());

        assertTrue(sw.toString().contains("(none)"));
    }
}
