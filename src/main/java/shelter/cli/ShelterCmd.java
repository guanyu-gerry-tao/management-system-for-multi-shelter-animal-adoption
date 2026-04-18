package shelter.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import shelter.domain.Shelter;

import java.io.PrintWriter;
import java.util.List;

/**
 * Top-level CLI command group for shelter management operations.
 * Provides subcommands to register, list, update, and remove shelters.
 * All operations are delegated to {@link AppContext#shelterApp()}.
 */
@Command(
        name = "shelter",
        description = "Manage shelters",
        subcommands = {
                ShelterCmd.ListCmd.class,
                ShelterCmd.RegisterCmd.class,
                ShelterCmd.UpdateCmd.class,
                ShelterCmd.RemoveCmd.class
        },
        mixinStandardHelpOptions = true
)
public class ShelterCmd implements Runnable {

    /**
     * Prints usage help when the subcommand group is invoked without a subcommand.
     * This method is called by Picocli when no subcommand is specified.
     */
    @Override
    public void run() {
        System.out.println("Usage: shelter shelter <subcommand> --help");
    }

    // -------------------------------------------------------------------------
    // list
    // -------------------------------------------------------------------------

    /**
     * Renders a list of shelters as a comma-headed, space-padded table to the given writer.
     * Empty input produces the header row followed by {@code (none)} on the next line.
     * Used by both {@code shelter shelter list} and {@code shelter print}.
     *
     * @param out      the writer to print to; must not be null
     * @param shelters the shelters to render; must not be null (may be empty)
     */
    static void renderList(PrintWriter out, List<Shelter> shelters) {
        out.printf("%-36s  %-20s  %-20s  %s%n",
                "ID,", "NAME,", "LOCATION,", "CAPACITY");
        if (shelters.isEmpty()) {
            out.println("(none)");
            out.flush();
            return;
        }
        for (Shelter s : shelters) {
            out.printf("%-36s  %-20s  %-20s  %d/%d%n",
                    s.getId(), s.getName(), s.getLocation(),
                    s.getCurrentCount(), s.getCapacity());
        }
        out.flush();
    }

    /**
     * Lists all registered shelters with their IDs, names, locations, and capacity usage.
     * Prints {@code (none)} if no shelters have been registered.
     */
    @Command(name = "list", description = "List all shelters", mixinStandardHelpOptions = true)
    static class ListCmd implements Runnable {

        /**
         * Executes the list operation by delegating to {@link ShelterCmd#renderList}.
         * Writes to stdout via a flushing {@link PrintWriter}.
         */
        @Override
        public void run() {
            List<Shelter> shelters = AppContext.get().shelterApp().listShelters();
            renderList(new PrintWriter(System.out, true), shelters);
        }
    }

    // -------------------------------------------------------------------------
    // register
    // -------------------------------------------------------------------------

    /**
     * Registers a new shelter with the specified name, location, and capacity.
     * Prints the new shelter's ID and details upon success.
     */
    @Command(name = "register", description = "Register a new shelter",
             mixinStandardHelpOptions = true)
    static class RegisterCmd implements Runnable {

        /** The shelter name; required. */
        @Option(names = "--name", required = true, description = "Shelter name")
        private String name;

        /** The shelter location; required. */
        @Option(names = "--location", required = true, description = "Shelter location")
        private String location;

        /** The maximum animal capacity; required. */
        @Option(names = "--capacity", required = true, description = "Maximum capacity")
        private int capacity;

        /**
         * Executes the registration and prints the new shelter's ID and details.
         * Prints an error message if registration fails (e.g., duplicate name/location).
         */
        @Override
        public void run() {
            try {
                Shelter s = AppContext.get().shelterApp()
                        .registerShelter(name, location, capacity);
                System.out.printf("Registered shelter: %s (id=%s)%n", s.getName(), s.getId());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    /**
     * Updates an existing shelter's name, location, or capacity.
     * Only the fields provided are changed; omitted fields retain their current values.
     */
    @Command(name = "update", description = "Update a shelter's details",
             mixinStandardHelpOptions = true)
    static class UpdateCmd implements Runnable {

        /** The ID of the shelter to update; required. */
        @Option(names = "--id", required = true, description = "Shelter ID")
        private String id;

        /** The new name; omit to keep current value. */
        @Option(names = "--name", description = "New name (omit to keep current)")
        private String name;

        /** The new location; omit to keep current value. */
        @Option(names = "--location", description = "New location (omit to keep current)")
        private String location;

        /** The new capacity; omit to keep current value. */
        @Option(names = "--capacity", description = "New capacity (omit to keep current)")
        private Integer capacity;

        /**
         * Executes the update and prints a confirmation with the updated field values.
         * Prints an error message if the shelter is not found.
         */
        @Override
        public void run() {
            try {
                Shelter s = AppContext.get().shelterApp()
                        .updateShelter(id, name, location, capacity);
                System.out.printf("Updated shelter: %s (id=%s)%n", s.getName(), s.getId());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // remove
    // -------------------------------------------------------------------------

    /**
     * Removes a shelter from the system by ID.
     * The shelter must have no animals and no pending transfer requests.
     */
    @Command(name = "remove", description = "Remove a shelter", mixinStandardHelpOptions = true)
    static class RemoveCmd implements Runnable {

        /** The ID of the shelter to remove; required. */
        @Option(names = "--id", required = true, description = "Shelter ID")
        private String id;

        /**
         * Executes the removal and prints a confirmation message.
         * Prints an error message if the shelter is not found or removal is blocked.
         */
        @Override
        public void run() {
            try {
                AppContext.get().shelterApp().removeShelter(id);
                System.out.println("Removed shelter: " + id);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}
