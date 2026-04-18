package shelter.cli;

import org.junit.jupiter.api.Test;
import shelter.domain.Staff;
import shelter.service.model.AuditEntry;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AuditCmd#renderLog(PrintWriter, List, int)}.
 * Verifies comma-headed output, the {@code (none)} empty marker, and the truncation footer.
 */
class AuditCmdRenderLogTest {

    /**
     * Builds a minimal audit entry for testing.
     */
    private static AuditEntry<Object> entry(String action, int i) {
        Staff staff = new Staff("admin");
        return new AuditEntry<>(staff, action + i, "target" + i,
                LocalDateTime.of(2026, 1, 1, 0, 0, i));
    }

    @Test
    void renderLog_withEntries_hasCommaHeader() {
        List<AuditEntry<?>> entries = new ArrayList<>();
        entries.add(entry("submitted ", 1));
        entries.add(entry("approved ", 2));

        StringWriter sw = new StringWriter();
        AuditCmd.renderLog(new PrintWriter(sw), entries, Integer.MAX_VALUE);

        String out = sw.toString();
        assertTrue(out.contains("TIMESTAMP,"));
        assertTrue(out.contains("ACTION,"));
        assertTrue(out.contains("approved "));
        assertFalse(out.contains("---"));
        assertFalse(out.contains("(showing last"),
                "no footer when all entries fit under the limit");
    }

    @Test
    void renderLog_empty_printsNoneMarker() {
        StringWriter sw = new StringWriter();
        AuditCmd.renderLog(new PrintWriter(sw), List.of(), Integer.MAX_VALUE);

        assertTrue(sw.toString().contains("(none)"));
    }

    @Test
    void renderLog_withLimit_truncatesAndPrintsFooter() {
        List<AuditEntry<?>> entries = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            entries.add(entry("action ", i));
        }

        StringWriter sw = new StringWriter();
        AuditCmd.renderLog(new PrintWriter(sw), entries, 20);

        String out = sw.toString();
        assertTrue(out.contains("(showing last 20 of 25 entries)"),
                "footer should report truncation counts");
        // The 20 most recent entries are indices 5..24; index 0 must NOT appear
        assertFalse(out.contains("action 0\n") || out.contains("action 0,"));
    }
}
