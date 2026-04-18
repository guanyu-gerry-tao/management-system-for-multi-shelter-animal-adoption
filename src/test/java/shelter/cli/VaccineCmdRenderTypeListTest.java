package shelter.cli;

import org.junit.jupiter.api.Test;
import shelter.domain.Species;
import shelter.domain.VaccineType;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link VaccineCmd#renderTypeList(PrintWriter, List)}.
 * Verifies comma-headed output and the {@code (none)} empty marker.
 */
class VaccineCmdRenderTypeListTest {

    @Test
    void renderTypeList_withTypes_hasCommaHeaderAndRow() {
        VaccineType rabies = new VaccineType("Rabies", Species.DOG, 365);

        StringWriter sw = new StringWriter();
        VaccineCmd.renderTypeList(new PrintWriter(sw), List.of(rabies));

        String out = sw.toString();
        assertTrue(out.contains("NAME,"));
        assertTrue(out.contains("Rabies"));
        assertFalse(out.contains("---"));
    }

    @Test
    void renderTypeList_empty_printsNoneMarker() {
        StringWriter sw = new StringWriter();
        VaccineCmd.renderTypeList(new PrintWriter(sw), List.of());

        assertTrue(sw.toString().contains("(none)"));
    }
}
