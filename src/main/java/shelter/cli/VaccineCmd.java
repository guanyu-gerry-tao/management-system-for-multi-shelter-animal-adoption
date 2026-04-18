package shelter.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import shelter.domain.Species;
import shelter.domain.VaccineType;
import shelter.service.model.OverdueVaccination;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

/**
 * Top-level CLI command group for vaccination management operations.
 * Provides subcommands to record vaccinations, check overdue status, and manage vaccine types.
 * All operations are delegated to {@link AppContext#vaccinationApp()}.
 */
@Command(
        name = "vaccine",
        description = "Manage vaccinations and vaccine types",
        subcommands = {
                VaccineCmd.ListCmd.class,
                VaccineCmd.RecordCmd.class,
                VaccineCmd.OverdueCmd.class,
                VaccineCmd.TypeCmd.class
        },
        mixinStandardHelpOptions = true
)
public class VaccineCmd implements Runnable {

    /**
     * Prints usage help when the subcommand group is invoked without a subcommand.
     * This method is called by Picocli when no subcommand is specified.
     */
    @Override
    public void run() {
        System.out.println("Usage: shelter vaccine <subcommand> --help");
    }

    // -------------------------------------------------------------------------
    // render helpers (shared by list subcommands and `shelter print`)
    // -------------------------------------------------------------------------

    /**
     * Renders a list of vaccine types as a comma-headed, space-padded table to the given writer.
     * Empty input produces the header row followed by {@code (none)} on the next line.
     * Used by both {@code shelter vaccine type list} and {@code shelter print}.
     *
     * @param out   the writer to print to; must not be null
     * @param types the vaccine types to render; must not be null (may be empty)
     */
    static void renderTypeList(PrintWriter out, List<VaccineType> types) {
        out.printf("%-36s  %-20s  %-10s  %s%n",
                "ID,", "NAME,", "SPECIES,", "VALIDITY (DAYS)");
        if (types.isEmpty()) {
            out.println("(none)");
            out.flush();
            return;
        }
        for (VaccineType t : types) {
            out.printf("%-36s  %-20s  %-10s  %d%n",
                    t.getId(), t.getName(), t.getApplicableSpecies(), t.getValidityDays());
        }
        out.flush();
    }

    /**
     * Renders vaccination records as a comma-headed, space-padded table to the given writer.
     * Empty input prints the header followed by {@code (none)}.
     * Used by both {@code shelter vaccine list} and {@code shelter print}.
     *
     * @param out   the writer to print to; must not be null
     * @param views the vaccination record views to render; must not be null (may be empty)
     */
    static void renderRecordList(PrintWriter out,
                                 List<shelter.application.model.VaccinationRecordView> views) {
        out.printf("%-36s  %-14s  %-8s  %-20s  %s%n",
                "ID,", "ANIMAL,", "SPECIES,", "VACCINE,", "DATE");
        if (views.isEmpty()) {
            out.println("(none)");
            out.flush();
            return;
        }
        for (shelter.application.model.VaccinationRecordView v : views) {
            out.printf("%-36s  %-14s  %-8s  %-20s  %s%n",
                    v.getRecord().getId(),
                    v.getAnimalName(),
                    v.getSpecies().name(),
                    v.getVaccineTypeName(),
                    v.getRecord().getDateAdministered());
        }
        out.flush();
    }

    // -------------------------------------------------------------------------
    // list (vaccination records)
    // -------------------------------------------------------------------------

    /**
     * Lists every vaccination record in the system, using {@link shelter.application.model.VaccinationRecordView}
     * to resolve animal and vaccine type display names. Used primarily for demo purposes
     * and by the {@code shelter print} summary.
     */
    @Command(name = "list", description = "List all vaccination records",
             mixinStandardHelpOptions = true)
    static class ListCmd implements Runnable {

        /**
         * Executes the list operation by delegating to {@link VaccineCmd#renderRecordList}.
         * Writes to stdout via a flushing {@link PrintWriter}.
         */
        @Override
        public void run() {
            try {
                renderRecordList(new PrintWriter(System.out, true),
                        AppContext.get().vaccinationApp().listAllVaccinationRecords());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // record
    // -------------------------------------------------------------------------

    /**
     * Records that an animal received a specific vaccine on a given date.
     * The vaccine type must exist in the catalog and apply to the animal's species.
     */
    @Command(name = "record", description = "Record a vaccination for an animal",
             mixinStandardHelpOptions = true)
    static class RecordCmd implements Runnable {

        /** The ID of the animal that was vaccinated; required. */
        @Option(names = "--animal", required = true, description = "Animal ID")
        private String animalId;

        /** The name of the vaccine type administered; required. */
        @Option(names = "--type", required = true, description = "Vaccine type name")
        private String vaccineTypeName;

        /** The date the vaccine was administered (yyyy-MM-dd); required. */
        @Option(names = "--date", required = true, description = "Date administered (yyyy-MM-dd)")
        private LocalDate date;

        /**
         * Executes the vaccination record and prints a confirmation message.
         * Prints an error if the animal is not found, vaccine type is unknown, or species mismatch.
         */
        @Override
        public void run() {
            try {
                AppContext.get().vaccinationApp()
                        .recordVaccination(animalId, vaccineTypeName, date);
                System.out.printf("Recorded vaccination: animal=%s, type=%s, date=%s%n",
                        animalId, vaccineTypeName, date);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // overdue
    // -------------------------------------------------------------------------

    /**
     * Checks which vaccinations are overdue for a given animal.
     * An overdue vaccination is one that is either missing or past its validity period.
     */
    @Command(name = "overdue",
             description = "List overdue vaccinations for an animal",
             mixinStandardHelpOptions = true)
    static class OverdueCmd implements Runnable {

        /** The ID of the animal to check; required. */
        @Option(names = "--animal", required = true, description = "Animal ID")
        private String animalId;

        /**
         * Executes the overdue check and prints each overdue vaccination.
         * Prints a confirmation message if all vaccinations are current.
         */
        @Override
        public void run() {
            try {
                List<OverdueVaccination> overdueList = AppContext.get().vaccinationApp()
                        .getOverdueVaccinations(animalId);
                if (overdueList.isEmpty()) {
                    System.out.println("All vaccinations are current for animal: " + animalId);
                    return;
                }
                System.out.printf("%-25s  %-12s%n", "Vaccine Type", "Due Date");
                System.out.println("-".repeat(40));
                for (OverdueVaccination ov : overdueList) {
                    System.out.printf("%-25s  %-12s%n",
                            ov.getVaccineType().getName(), ov.getDueDate());
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // type (sub-group: list / add / update / remove)
    // -------------------------------------------------------------------------

    /**
     * Subcommand group for vaccine type catalog management.
     * Provides add, update, remove, and list operations for vaccine types.
     */
    @Command(
            name = "type",
            description = "Manage vaccine types",
            subcommands = {
                    TypeCmd.ListCmd.class,
                    TypeCmd.AddCmd.class,
                    TypeCmd.UpdateCmd.class,
                    TypeCmd.RemoveCmd.class
            },
            mixinStandardHelpOptions = true
    )
    static class TypeCmd implements Runnable {

        /**
         * Prints usage help when invoked without a subcommand.
         * This method is called by Picocli when no nested subcommand is specified.
         */
        @Override
        public void run() {
            System.out.println("Usage: shelter vaccine type <subcommand> --help");
        }

        /**
         * Lists all vaccine types in the catalog with their IDs, names, species, and validity periods.
         * Prints {@code (none)} if the catalog is empty.
         */
        @Command(name = "list", description = "List all vaccine types",
                 mixinStandardHelpOptions = true)
        static class ListCmd implements Runnable {

            /**
             * Executes the list operation by delegating to {@link VaccineCmd#renderTypeList}.
             * Writes to stdout via a flushing {@link PrintWriter}.
             */
            @Override
            public void run() {
                List<VaccineType> types = AppContext.get().vaccinationApp().listVaccineTypes();
                renderTypeList(new PrintWriter(System.out, true), types);
            }
        }

        /**
         * Adds a new vaccine type to the catalog with a name, applicable species, and validity period.
         * Throws an error if a vaccine type with the same name already exists.
         */
        @Command(name = "add", description = "Add a new vaccine type to the catalog",
                 mixinStandardHelpOptions = true)
        static class AddCmd implements Runnable {

            /** The vaccine type name; required. */
            @Option(names = "--name", required = true, description = "Vaccine type name")
            private String name;

            /** The applicable species; required. */
            @Option(names = "--species", required = true,
                    description = "Applicable species: DOG, CAT, RABBIT, OTHER")
            private Species species;

            /** The validity period in days; required. */
            @Option(names = "--days", required = true, description = "Validity period in days")
            private int days;

            /**
             * Executes the add operation and prints the new vaccine type's ID and name.
             * Prints an error if a duplicate name exists.
             */
            @Override
            public void run() {
                try {
                    VaccineType t = AppContext.get().vaccinationApp()
                            .addVaccineType(name, species, days);
                    System.out.printf("Added vaccine type: %s (id=%s, species=%s, validity=%d days)%n",
                            t.getName(), t.getId(), t.getApplicableSpecies(), t.getValidityDays());
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }

        /**
         * Updates an existing vaccine type's name, species, or validity period by ID.
         * Only provided fields are changed; omitted fields retain their current values.
         */
        @Command(name = "update", description = "Update a vaccine type",
                 mixinStandardHelpOptions = true)
        static class UpdateCmd implements Runnable {

            /** The ID of the vaccine type to update; required. */
            @Option(names = "--id", required = true, description = "Vaccine type ID")
            private String id;

            /** New name; omit to keep current value. */
            @Option(names = "--name", description = "New name (omit to keep current)")
            private String name;

            /** New applicable species; omit to keep current value. */
            @Option(names = "--species", description = "New species (omit to keep current)")
            private Species species;

            /** New validity period; omit to keep current value. */
            @Option(names = "--days", description = "New validity days (omit to keep current)")
            private Integer days;

            /**
             * Executes the update and prints a confirmation message.
             * Prints an error if the vaccine type is not found or the new name conflicts.
             */
            @Override
            public void run() {
                try {
                    VaccineType t = AppContext.get().vaccinationApp()
                            .updateVaccineType(id, name, species, days);
                    System.out.printf("Updated vaccine type: %s (id=%s)%n", t.getName(), t.getId());
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }

        /**
         * Removes a vaccine type from the catalog by ID.
         * Throws an error if the vaccine type is not found.
         */
        @Command(name = "remove", description = "Remove a vaccine type from the catalog",
                 mixinStandardHelpOptions = true)
        static class RemoveCmd implements Runnable {

            /** The ID of the vaccine type to remove; required. */
            @Option(names = "--id", required = true, description = "Vaccine type ID")
            private String id;

            /**
             * Executes the removal and prints a confirmation message.
             * Prints an error if the vaccine type is not found.
             */
            @Override
            public void run() {
                try {
                    AppContext.get().vaccinationApp().removeVaccineType(id);
                    System.out.println("Removed vaccine type: " + id);
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }
        }
    }
}
