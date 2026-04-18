package shelter.cli;

import picocli.CommandLine.Command;
import shelter.service.model.AuditEntry;

import java.io.PrintWriter;
import java.util.List;

/**
 * Top-level CLI command group for audit log operations.
 * Provides a subcommand to view the full persistent audit trail of staff actions.
 * All operations are delegated to {@link AppContext#auditApp()}.
 */
@Command(
        name = "audit",
        description = "View the system audit log",
        subcommands = {AuditCmd.LogCmd.class},
        mixinStandardHelpOptions = true
)
public class AuditCmd implements Runnable {

    /**
     * Creates a new AuditCmd instance.
     * Picocli instantiates command classes reflectively via the no-arg constructor.
     */
    public AuditCmd() {}

    /**
     * Prints usage help when the subcommand group is invoked without a subcommand.
     * This method is called by Picocli when no subcommand is specified.
     */
    @Override
    public void run() {
        System.out.println("Usage: shelter audit log");
    }

    // -------------------------------------------------------------------------
    // log
    // -------------------------------------------------------------------------

    /**
     * Renders the audit log as a comma-headed, space-padded table to the given writer.
     * Empty input produces the header row followed by {@code (none)} on the next line.
     * If {@code entries.size() > limit}, only the last {@code limit} entries are printed
     * and a truncation footer of the form {@code (showing last N of M entries)} follows.
     * Used by both {@code shelter audit log} and {@code shelter print}.
     *
     * @param out     the writer to print to; must not be null
     * @param entries the audit entries to render; must not be null (may be empty)
     * @param limit   the maximum number of most-recent entries to show; must be positive
     */
    public static void renderLog(PrintWriter out, List<AuditEntry<?>> entries, int limit) {
        out.printf("%-20s  %-12s  %-30s  %s%n",
                "TIMESTAMP,", "STAFF,", "ACTION,", "TARGET");
        if (entries.isEmpty()) {
            out.println("(none)");
            out.flush();
            return;
        }
        // When the list exceeds the limit, print only the tail (most recent)
        int start = Math.max(0, entries.size() - limit);
        for (int i = start; i < entries.size(); i++) {
            AuditEntry<?> e = entries.get(i);
            String staffName = e.getStaff() != null ? e.getStaff().getName() : "unknown";
            String target    = e.getTarget() != null ? e.getTarget().toString() : "";
            // Truncate long target strings for readability
            if (target.length() > 35) {
                target = target.substring(0, 32) + "...";
            }
            out.printf("%-20s  %-12s  %-30s  %s%n",
                    e.getTimestamp(), staffName, e.getAction(), target);
        }
        // Footer showing truncation counts only when we actually truncated
        if (start > 0) {
            out.println("(showing last " + limit + " of " + entries.size() + " entries)");
        }
        out.flush();
    }

    /**
     * Displays the full persistent audit log of all staff actions.
     * Reads from the CSV-backed audit repository so the history survives across command executions.
     */
    @Command(name = "log", description = "Display the full audit log",
             mixinStandardHelpOptions = true)
    static class LogCmd implements Runnable {

        /**
         * Executes the log retrieval by delegating to {@link AuditCmd#renderLog}.
         * The full log is printed with no truncation; large logs use the {@code shelter print}
         * summary to get a capped view.
         */
        @Override
        public void run() {
            List<AuditEntry<?>> entries = AppContext.get().auditApp().getLog();
            renderLog(new PrintWriter(System.out, true), entries, Integer.MAX_VALUE);
        }
    }
}
