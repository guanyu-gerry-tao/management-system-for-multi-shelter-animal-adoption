package shelter.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import shelter.cli.print.DataDirHash;
import shelter.cli.print.MarkdownRenderer;
import shelter.cli.print.SnapshotRenderer;
import shelter.startup.SystemStartupImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Top-level CLI command that renders the full system state.
 * Without flags, prints a one-shot 8-section snapshot to stdout.
 * With {@code --watch}, polls the data directory and rewrites a markdown file
 * whenever the CSV contents change. Used during the class demo to drive a
 * live-updating VS Code preview pane.
 */
@Command(
        name = "print",
        description = "Print the full system state; optionally watch for changes and update a file",
        mixinStandardHelpOptions = true
)
public class PrintCmd implements Runnable {

    /**
     * Creates a new PrintCmd instance.
     * Picocli instantiates command classes reflectively via the no-arg constructor.
     */
    public PrintCmd() {}

    private static final String DEFAULT_OUT_NAME = "dashboard.md";
    private static final long POLL_INTERVAL_MS = 1000L;
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** When set, run the watch loop instead of printing once and exiting. */
    @Option(names = "--watch",
            description = "Keep running, re-rendering the output file when CSVs change")
    private boolean watch;

    /** Output path for the markdown dashboard in watch mode. */
    @Option(names = "--out",
            description = "Output path for the markdown dashboard (watch mode only; default: <shelter-home>/dashboard.md)")
    private Path out;

    /**
     * Either prints a one-shot snapshot to stdout or enters the watch loop.
     * Errors are printed to stderr.
     */
    @Override
    public void run() {
        if (!watch) {
            // One-shot mode: render the plain-text snapshot and return
            new SnapshotRenderer(AppContext.get()).render(new PrintWriter(System.out, true));
            return;
        }
        try {
            runWatchLoop();
        } catch (InterruptedException ie) {
            // Preserve the interrupt so the JVM shuts down cleanly on Ctrl+C
            Thread.currentThread().interrupt();
        } catch (IOException ioe) {
            System.err.println("Error: " + ioe.getMessage());
        }
    }

    /**
     * Polls the shelter data directory once per second and rewrites the output markdown
     * when the SHA-1 digest of CSV contents changes. Writes atomically via a temp file.
     *
     * @throws IOException          if the data directory cannot be read or the output cannot be written
     * @throws InterruptedException if the polling sleep is interrupted
     */
    private void runWatchLoop() throws IOException, InterruptedException {
        Path shelterHome = resolveShelterHome();
        Path dataDir = shelterHome.resolve("data");
        Path outPath = (out != null) ? out : shelterHome.resolve(DEFAULT_OUT_NAME);

        // Ensure the data directory exists so DataDirHash does not fail on a fresh install
        Files.createDirectories(dataDir);
        // Ensure the parent of the output file exists so atomic rename has somewhere to land
        if (outPath.getParent() != null) {
            Files.createDirectories(outPath.getParent());
        }

        System.out.println("Watching " + dataDir + ", writing " + outPath + " (Ctrl+C to stop)");

        String lastHash = null;
        while (!Thread.currentThread().isInterrupted()) {
            String currentHash = DataDirHash.compute(dataDir);
            // Only rewrite when the CSV byte-content has actually changed
            if (!currentHash.equals(lastHash)) {
                writeAtomically(outPath, renderFreshMarkdown(shelterHome));
                System.out.println("[" + LocalTime.now().format(CLOCK) + "] updated");
                lastHash = currentHash;
            }
            Thread.sleep(POLL_INTERVAL_MS);
        }
    }

    /**
     * Builds a fresh {@link SystemStartupImpl} bound to {@code shelterHome} and renders
     * the markdown dashboard from it. Rebuilding the graph each tick ensures the dashboard
     * reflects CSV writes made by other {@code shelter} processes during the watch,
     * avoiding stale in-memory state from the cached CLI application graph.
     *
     * @param shelterHome the base shelter home directory
     * @return the rendered markdown dashboard
     */
    private static String renderFreshMarkdown(Path shelterHome) {
        SystemStartupImpl fresh = new SystemStartupImpl(shelterHome);
        fresh.initialize();
        SnapshotRenderer snap = new SnapshotRenderer(
                () -> fresh.shelterApp().listShelters(),
                () -> fresh.animalApp().listAnimalsWithShelterName(null),
                () -> fresh.adopterApp().listAdopters(),
                () -> fresh.adoptionApp().listAllRequests(),
                () -> fresh.transferApp().listAllTransfers(),
                () -> fresh.vaccinationApp().listVaccineTypes(),
                () -> fresh.vaccinationApp().listAllVaccinationRecords(),
                () -> fresh.auditApp().getLog()
        );
        return new MarkdownRenderer(snap).render();
    }

    /**
     * Writes {@code content} to {@code target} via a temp file + atomic rename so
     * the VS Code file watcher never sees a half-written file. Falls back to a
     * non-atomic move if the filesystem does not support atomic moves.
     *
     * @param target  the destination path
     * @param content the content to write
     * @throws IOException if the temp file cannot be written or moved into place
     */
    private static void writeAtomically(Path target, String content) throws IOException {
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        Files.writeString(tmp, content);
        try {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException fallback) {
            // Some filesystems (notably Windows shares) do not support atomic moves; degrade gracefully
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Resolves the shelter home directory in the same way {@code Main} does:
     * {@code $SHELTER_HOME} if set, otherwise {@code ~/shelter}.
     *
     * @return the resolved shelter home path
     */
    private static Path resolveShelterHome() {
        String env = System.getenv("SHELTER_HOME");
        return (env != null) ? Path.of(env) : Path.of(System.getProperty("user.home"), "shelter");
    }
}
