package shelter.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import shelter.domain.ActivityLevel;
import shelter.domain.Adopter;
import shelter.domain.DailySchedule;
import shelter.domain.LivingSpace;
import shelter.domain.Species;

import java.util.List;

/**
 * Top-level CLI command group for adopter management operations.
 * Provides subcommands to register, list, update, and remove adopters.
 * All operations are delegated to {@link AppContext#adopterApp()}.
 */
@Command(
        name = "adopter",
        description = "Manage adopters",
        subcommands = {
                AdopterCmd.ListCmd.class,
                AdopterCmd.RegisterCmd.class,
                AdopterCmd.UpdateCmd.class,
                AdopterCmd.RemoveCmd.class
        },
        mixinStandardHelpOptions = true
)
public class AdopterCmd implements Runnable {

    /**
     * Prints usage help when the subcommand group is invoked without a subcommand.
     * This method is called by Picocli when no subcommand is specified.
     */
    @Override
    public void run() {
        System.out.println("Usage: shelter adopter <subcommand> --help");
    }

    // -------------------------------------------------------------------------
    // list
    // -------------------------------------------------------------------------

    /**
     * Lists all registered adopters with their IDs, names, living spaces, and schedules.
     * Prints a message if no adopters have been registered.
     */
    @Command(name = "list", description = "List all adopters", mixinStandardHelpOptions = true)
    static class ListCmd implements Runnable {

        /**
         * Executes the list operation and prints each adopter's summary to stdout.
         * Prints a message if no adopters are found.
         */
        @Override
        public void run() {
            List<Adopter> adopters = AppContext.get().adopterApp().listAdopters();
            if (adopters.isEmpty()) {
                System.out.println("No adopters registered.");
                return;
            }
            System.out.printf("%-36s  %-15s  %-18s  %-22s%n",
                    "ID", "Name", "Living Space", "Schedule");
            System.out.println("-".repeat(100));
            for (Adopter a : adopters) {
                System.out.printf("%-36s  %-15s  %-18s  %-22s%n",
                        a.getId(), a.getName(), a.getLivingSpace(), a.getDailySchedule());
            }
        }
    }

    // -------------------------------------------------------------------------
    // register
    // -------------------------------------------------------------------------

    /**
     * Registers a new adopter with personal details and optional preferences.
     * Preference fields default to null (no preference) when omitted.
     */
    @Command(name = "register", description = "Register a new adopter",
             mixinStandardHelpOptions = true)
    static class RegisterCmd implements Runnable {

        /** The adopter's name; required. */
        @Option(names = "--name", required = true, description = "Adopter name")
        private String name;

        /** The adopter's living space type; required. */
        @Option(names = "--space", required = true,
                description = "Living space: APARTMENT, HOUSE_NO_YARD, HOUSE_WITH_YARD")
        private LivingSpace livingSpace;

        /** The adopter's daily schedule; required. */
        @Option(names = "--schedule", required = true,
                description = "Schedule: HOME_MOST_OF_DAY, AWAY_PART_OF_DAY, AWAY_MOST_OF_DAY")
        private DailySchedule dailySchedule;

        /** Preferred species; omit for no preference. */
        @Option(names = "--species", description = "Preferred species (omit for no preference)")
        private Species preferredSpecies;

        /** Preferred breed; omit for no preference. */
        @Option(names = "--breed", description = "Preferred breed (omit for no preference)")
        private String preferredBreed;

        /** Preferred activity level; omit for no preference. */
        @Option(names = "--activity",
                description = "Preferred activity level (omit for no preference)")
        private ActivityLevel preferredActivityLevel;

        /** Whether vaccinated animals are required; omit for no preference. */
        @Option(names = "--requires-vaccinated",
                description = "Whether vaccinated animals are required: true or false")
        private Boolean requiresVaccinated;

        /** Minimum preferred animal age; defaults to 0. */
        @Option(names = "--min-age", description = "Minimum preferred animal age (default 0)")
        private int minAge = 0;

        /** Maximum preferred animal age; defaults to 20. */
        @Option(names = "--max-age", description = "Maximum preferred animal age (default 20)")
        private int maxAge = 20;

        /**
         * Executes the registration and prints the new adopter's ID and name.
         * Prints an error message if registration fails.
         */
        @Override
        public void run() {
            try {
                Adopter a = AppContext.get().adopterApp().registerAdopter(
                        name, livingSpace, dailySchedule,
                        preferredSpecies, preferredBreed, preferredActivityLevel,
                        requiresVaccinated,
                        minAge, maxAge);
                System.out.printf("Registered adopter: %s (id=%s)%n", a.getName(), a.getId());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    /**
     * Updates an existing adopter's personal details or preferences.
     * Only the fields provided are changed; omitted fields retain their current values.
     */
    @Command(name = "update", description = "Update an adopter's details",
             mixinStandardHelpOptions = true)
    static class UpdateCmd implements Runnable {

        /** The ID of the adopter to update; required. */
        @Option(names = "--id", required = true, description = "Adopter ID")
        private String id;

        /** New name; omit to keep current value. */
        @Option(names = "--name", description = "New name (omit to keep current)")
        private String name;

        /** New living space; omit to keep current value. */
        @Option(names = "--space", description = "New living space (omit to keep current)")
        private LivingSpace livingSpace;

        /** New daily schedule; omit to keep current value. */
        @Option(names = "--schedule", description = "New schedule (omit to keep current)")
        private DailySchedule dailySchedule;

        /** New preferred species; omit to keep current value. */
        @Option(names = "--species", description = "New preferred species (omit to keep current)")
        private Species preferredSpecies;

        /** New preferred breed; omit to keep current value. */
        @Option(names = "--breed", description = "New preferred breed (omit to keep current)")
        private String preferredBreed;

        /** New preferred activity level; omit to keep current value. */
        @Option(names = "--activity",
                description = "New preferred activity level (omit to keep current)")
        private ActivityLevel preferredActivityLevel;

        /** New vaccination requirement; omit to keep current value. */
        @Option(names = "--requires-vaccinated",
                description = "New vaccination requirement: true or false (omit to keep current)")
        private Boolean requiresVaccinated;

        /** New minimum preferred age; omit to keep current value. */
        @Option(names = "--min-age", description = "New minimum preferred age (omit to keep current)")
        private Integer minAge;

        /** New maximum preferred age; omit to keep current value. */
        @Option(names = "--max-age", description = "New maximum preferred age (omit to keep current)")
        private Integer maxAge;

        /**
         * Executes the update and prints a confirmation message.
         * Prints an error message if the adopter is not found.
         */
        @Override
        public void run() {
            try {
                Adopter a = AppContext.get().adopterApp().updateAdopter(
                        id, name, livingSpace, dailySchedule,
                        preferredSpecies, preferredBreed, preferredActivityLevel,
                        requiresVaccinated,
                        minAge, maxAge);
                System.out.printf("Updated adopter: %s (id=%s)%n", a.getName(), a.getId());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // remove
    // -------------------------------------------------------------------------

    /**
     * Removes an adopter from the system by ID.
     * The adopter must not have a pending adoption request.
     */
    @Command(name = "remove", description = "Remove an adopter", mixinStandardHelpOptions = true)
    static class RemoveCmd implements Runnable {

        /** The ID of the adopter to remove; required. */
        @Option(names = "--id", required = true, description = "Adopter ID")
        private String id;

        /**
         * Executes the removal and prints a confirmation message.
         * Prints an error message if the adopter is not found or removal is blocked.
         */
        @Override
        public void run() {
            try {
                AppContext.get().adopterApp().removeAdopter(id);
                System.out.println("Removed adopter: " + id);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}
