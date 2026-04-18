package shelter.cli.print;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link MarkdownRenderer}. Verifies the top-level heading, timestamp,
 * all eight section subheadings, and that each section is wrapped in a fenced code block.
 */
class MarkdownRendererTest {

    private MarkdownRenderer newEmptyRenderer() {
        SnapshotRenderer snap = new SnapshotRenderer(
                List::of, List::of, List::of, List::of,
                List::of, List::of, List::of, List::of);
        return new MarkdownRenderer(snap);
    }

    @Test
    void render_hasTopLevelHeadingAndUpdatedTimestamp() {
        String md = newEmptyRenderer().render();
        assertTrue(md.startsWith("# Shelter System State"));
        assertTrue(md.contains("*Updated:"));
    }

    @Test
    void render_wrapsEachSectionInFencedCodeBlock() {
        String md = newEmptyRenderer().render();
        assertTrue(md.contains("## Shelters"));
        assertTrue(md.contains("## Animals"));
        assertTrue(md.contains("## Adopters"));
        assertTrue(md.contains("## Adoption Requests"));
        assertTrue(md.contains("## Transfer Requests"));
        assertTrue(md.contains("## Vaccine Types"));
        assertTrue(md.contains("## Vaccinations"));
        assertTrue(md.contains("## Audit Log"));
        // Each section emits one opening and one closing fence => at least 16 fence lines
        long fences = md.lines().filter(line -> line.equals("```")).count();
        assertTrue(fences >= 16, "Expected at least 16 fence lines, got " + fences);
    }
}
