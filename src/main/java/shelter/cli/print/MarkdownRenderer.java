package shelter.cli.print;

import shelter.cli.AppContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Renders the full system snapshot as markdown suitable for VS Code's preview pane.
 * Structure: a top-level {@code # Shelter System State} heading, an {@code *Updated: ...*}
 * timestamp, then eight {@code ## Section} headings, each followed by a fenced code block
 * containing the plain-text table produced by {@link SnapshotRenderer}.
 */
public final class MarkdownRenderer {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Section headings used in the markdown document, in order. */
    private static final String[] SECTIONS = {
            "Shelters", "Animals", "Adopters", "Adoption Requests",
            "Transfer Requests", "Vaccine Types", "Vaccinations", "Audit Log"
    };

    /** Plain-text section titles emitted by {@link SnapshotRenderer}, aligned with {@link #SECTIONS}. */
    private static final String[] TITLES = {
            "SHELTERS", "ANIMALS", "ADOPTERS", "ADOPTION REQUESTS",
            "TRANSFER REQUESTS", "VACCINE TYPES", "VACCINATIONS", "AUDIT LOG"
    };

    private final SnapshotRenderer snapshotRenderer;

    /**
     * Constructs a markdown renderer backed by the given snapshot renderer.
     * This constructor enables dependency injection in unit tests.
     *
     * @param snapshotRenderer the plain-text snapshot renderer to wrap; must not be null
     */
    public MarkdownRenderer(SnapshotRenderer snapshotRenderer) {
        if (snapshotRenderer == null) {
            throw new IllegalArgumentException("SnapshotRenderer must not be null.");
        }
        this.snapshotRenderer = snapshotRenderer;
    }

    /**
     * Convenience constructor that wires a {@link SnapshotRenderer} backed by the CLI context.
     *
     * @param ctx the context used to fetch live data; must not be null
     */
    public MarkdownRenderer(AppContext ctx) {
        this(new SnapshotRenderer(ctx));
    }

    /**
     * Produces the full markdown document as a single string.
     * Each invocation reads current state fresh through the snapshot renderer.
     *
     * @return the rendered markdown document
     */
    public String render() {
        StringBuilder doc = new StringBuilder();
        doc.append("# Shelter System State\n\n");
        doc.append("*Updated: ").append(LocalDateTime.now().format(STAMP)).append("*\n\n");

        // Produce the full plain-text snapshot once, then split by section title for markdown wrapping
        StringWriter sw = new StringWriter();
        snapshotRenderer.render(new PrintWriter(sw));
        String snapshot = sw.toString();

        for (int i = 0; i < SECTIONS.length; i++) {
            String body = extractSection(snapshot, TITLES[i]);
            doc.append("## ").append(SECTIONS[i]).append("\n\n");
            doc.append("```\n").append(body).append("```\n\n");
        }
        return doc.toString();
    }

    /**
     * Extracts the body text of the named section from a plain-text snapshot.
     * The section body is everything between {@code === TITLE ===} and the next
     * {@code === } marker (or end-of-document), exclusive of both delimiters and
     * surrounding blank lines. Returned body always ends with a newline.
     */
    private static String extractSection(String snapshot, String title) {
        String marker = "=== " + title + " ===";
        int start = snapshot.indexOf(marker);
        if (start < 0) {
            return "";
        }
        int bodyStart = snapshot.indexOf('\n', start) + 1;
        int bodyEnd = snapshot.indexOf("=== ", bodyStart);
        if (bodyEnd < 0) {
            bodyEnd = snapshot.length();
        }
        String body = snapshot.substring(bodyStart, bodyEnd);
        // Trim leading blank lines and collapse trailing blank lines to a single newline
        while (body.startsWith("\n")) {
            body = body.substring(1);
        }
        while (body.endsWith("\n\n")) {
            body = body.substring(0, body.length() - 1);
        }
        if (!body.endsWith("\n")) {
            body = body + "\n";
        }
        return body;
    }
}
