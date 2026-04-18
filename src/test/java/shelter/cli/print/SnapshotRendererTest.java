package shelter.cli.print;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SnapshotRenderer}. Verifies that all eight section titles appear
 * in the documented order regardless of underlying data contents, and that the
 * supplier-based constructor allows data to be injected without touching the CLI singleton.
 */
class SnapshotRendererTest {

    @Test
    void render_emitsAllEightSectionTitlesInOrder() {
        StringWriter sw = new StringWriter();
        SnapshotRenderer renderer = new SnapshotRenderer(
                List::of, List::of, List::of, List::of,
                List::of, List::of, List::of, List::of);
        renderer.render(new PrintWriter(sw));
        String out = sw.toString();

        int iShelters  = out.indexOf("=== SHELTERS ===");
        int iAnimals   = out.indexOf("=== ANIMALS ===");
        int iAdopters  = out.indexOf("=== ADOPTERS ===");
        int iAdoptReq  = out.indexOf("=== ADOPTION REQUESTS ===");
        int iXferReq   = out.indexOf("=== TRANSFER REQUESTS ===");
        int iVacTypes  = out.indexOf("=== VACCINE TYPES ===");
        int iVacs      = out.indexOf("=== VACCINATIONS ===");
        int iAudit     = out.indexOf("=== AUDIT LOG ===");

        for (int idx : new int[]{iShelters, iAnimals, iAdopters, iAdoptReq,
                iXferReq, iVacTypes, iVacs, iAudit}) {
            assertTrue(idx >= 0, "Missing section in output: " + out);
        }
        assertTrue(iShelters < iAnimals);
        assertTrue(iAnimals < iAdopters);
        assertTrue(iAdopters < iAdoptReq);
        assertTrue(iAdoptReq < iXferReq);
        assertTrue(iXferReq < iVacTypes);
        assertTrue(iVacTypes < iVacs);
        assertTrue(iVacs < iAudit);
    }

    @Test
    void render_emptyDataSources_eachSectionPrintsNoneMarker() {
        StringWriter sw = new StringWriter();
        SnapshotRenderer renderer = new SnapshotRenderer(
                List::of, List::of, List::of, List::of,
                List::of, List::of, List::of, List::of);
        renderer.render(new PrintWriter(sw));

        // Every section reports (none) when its data source is empty
        long noneCount = sw.toString().lines().filter(l -> l.equals("(none)")).count();
        assertTrue(noneCount >= 8, "Expected (none) in every section, got " + noneCount);
    }
}
