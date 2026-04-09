package shelter.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import shelter.domain.ActivityLevel;
import shelter.domain.Animal;
import shelter.domain.Dog;
import shelter.domain.Rabbit;

import java.time.LocalDate;
import java.util.List;

/**
 * Top-level CLI command group for animal management operations.
 * Provides subcommands to admit, list, update, and remove animals.
 * All operations are delegated to {@link AppContext#animalApp()}.
 */
@Command(
        name = "animal",
        description = "Manage animals",
        subcommands = {
                AnimalCmd.ListCmd.class,
                AnimalCmd.AdmitCmd.class,
                AnimalCmd.UpdateCmd.class,
                AnimalCmd.RemoveCmd.class
        },
        mixinStandardHelpOptions = true
)
public class AnimalCmd implements Runnable {

    /**
     * Prints usage help when the subcommand group is invoked without a subcommand.
     * This method is called by Picocli when no subcommand is specified.
     */
    @Override
    public void run() {
        System.out.println("Usage: shelter animal <subcommand> --help");
    }

    // -------------------------------------------------------------------------
    // list
    // -------------------------------------------------------------------------

    /**
     * Lists animals in the system, optionally filtered to a specific shelter.
     * Without {@code --shelter}, all animals system-wide are returned.
     */
    @Command(name = "list", description = "List animals (optionally by shelter)",
             mixinStandardHelpOptions = true)
    static class ListCmd implements Runnable {

        /** Shelter ID filter; omit to list all animals across all shelters. */
        @Option(names = "--shelter", description = "Shelter ID (omit for all shelters)")
        private String shelterId;

        /**
         * Executes the list operation and prints each animal's details to stdout.
         * Prints a message if no animals are found.
         */
        @Override
        public void run() {
            try {
                List<Animal> animals = AppContext.get().animalApp().listAnimals(shelterId);
                if (animals.isEmpty()) {
                    System.out.println("No animals found.");
                    return;
                }
                System.out.printf("%-36s  %-10s  %-12s  %-20s  %-4s  %-8s  %s%n",
                        "ID", "Species", "Name", "Breed", "Age", "Activity", "Status");
                System.out.println("-".repeat(110));
                for (Animal a : animals) {
                    String status = a.isAvailable() ? "available" : "adopted";
                    System.out.printf("%-36s  %-10s  %-12s  %-20s  %-4d  %-8s  %s%n",
                            a.getId(), a.getSpecies(), a.getName(), a.getBreed(),
                            a.getAge(), a.getActivityLevel(), status);
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // admit
    // -------------------------------------------------------------------------

    /**
     * Admits a new animal into a shelter.
     * The species is specified with {@code --species} and routes to the appropriate admit method.
     * Species-specific flags (size, fur, indoor, etc.) are optional with sensible defaults.
     * Either {@code --birthday} or {@code --age} must be provided to indicate the animal's age.
     */
    @Command(name = "admit", description = "Admit a new animal into a shelter",
             mixinStandardHelpOptions = true)
    static class AdmitCmd implements Runnable {

        /** The species of the animal; required. Valid values: dog, cat, rabbit, other. */
        @Option(names = "--species", required = true,
                description = "Species: dog, cat, rabbit, other")
        private String species;

        /** The animal's name; required. */
        @Option(names = "--name", required = true, description = "Animal name")
        private String name;

        /** The animal's breed or description; required. */
        @Option(names = "--breed", required = true, description = "Breed or description")
        private String breed;

        /** The animal's date of birth in ISO format (yyyy-MM-dd); takes priority over --age. */
        @Option(names = "--birthday", description = "Date of birth (yyyy-MM-dd)")
        private LocalDate birthday;

        /** The animal's approximate age in years; converted to a birthday if --birthday is absent. */
        @Option(names = "--age", description = "Age in years (alternative to --birthday)")
        private Integer age;

        /** The animal's activity level; required. Valid values: LOW, MEDIUM, HIGH. */
        @Option(names = "--activity", required = true,
                description = "Activity level: LOW, MEDIUM, HIGH")
        private ActivityLevel activityLevel;

        /** The ID of the shelter to admit the animal into; required. */
        @Option(names = "--shelter", required = true, description = "Shelter ID")
        private String shelterId;

        /** Dog size; used only when species is dog. Defaults to MEDIUM. */
        @Option(names = "--size", description = "Dog size: SMALL, MEDIUM, LARGE (dogs only)")
        private Dog.Size size = Dog.Size.MEDIUM;

        /** Whether the dog or cat is neutered; used for dogs and cats. */
        @Option(names = "--neutered", description = "Whether the animal is neutered (dogs/cats)")
        private boolean neutered;

        /** Whether the cat lives indoors; used only when species is cat. */
        @Option(names = "--indoor", description = "Whether the cat is indoor (cats only)")
        private boolean indoor;

        /** Rabbit fur length; used only when species is rabbit. Defaults to SHORT. */
        @Option(names = "--fur", description = "Rabbit fur length: SHORT, LONG (rabbits only)")
        private Rabbit.FurLength furLength = Rabbit.FurLength.SHORT;

        /** Free-form species name for unclassified animals; used only when species is other. */
        @Option(names = "--species-name",
                description = "Species description for 'other' (e.g. fish)")
        private String speciesName = "unknown";

        /**
         * Executes the admit operation by routing to the appropriate typed admit method.
         * Prints the new animal's ID and details upon success.
         * Prints an error message if the shelter is not found, is at capacity, or input is invalid.
         */
        @Override
        public void run() {
            // Resolve birthday: prefer --birthday; fall back to converting --age to an approximate date
            LocalDate resolvedBirthday = birthday;
            if (resolvedBirthday == null) {
                if (age == null || age < 0) {
                    System.err.println("Error: provide --birthday (yyyy-MM-dd) or --age <years>.");
                    return;
                }
                resolvedBirthday = LocalDate.now().minusYears(age);
            }

            try {
                Animal animal;
                switch (species.toLowerCase()) {
                    case "dog" -> animal = AppContext.get().animalApp()
                            .admitDog(name, breed, resolvedBirthday, activityLevel,
                                      shelterId, size, neutered);
                    case "cat" -> animal = AppContext.get().animalApp()
                            .admitCat(name, breed, resolvedBirthday, activityLevel,
                                      shelterId, indoor, neutered);
                    case "rabbit" -> animal = AppContext.get().animalApp()
                            .admitRabbit(name, breed, resolvedBirthday, activityLevel,
                                         shelterId, furLength);
                    case "other" -> animal = AppContext.get().animalApp()
                            .admitOther(name, breed, resolvedBirthday, activityLevel,
                                        shelterId, speciesName);
                    default -> {
                        System.err.println("Error: unknown species '" + species
                                + "'. Valid values: dog, cat, rabbit, other.");
                        return;
                    }
                }
                System.out.printf("Admitted %s: %s (id=%s, shelter=%s)%n",
                        animal.getSpecies(), animal.getName(), animal.getId(), shelterId);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    /**
     * Updates an existing animal's mutable fields.
     * Breed and birthday are immutable; only name and activity level can be changed.
     */
    @Command(name = "update", description = "Update an animal's mutable details",
             mixinStandardHelpOptions = true)
    static class UpdateCmd implements Runnable {

        /** The ID of the animal to update; required. */
        @Option(names = "--id", required = true, description = "Animal ID")
        private String id;

        /** The new name; omit to keep current value. */
        @Option(names = "--name", description = "New name (omit to keep current)")
        private String name;

        /** The new activity level; omit to keep current value. */
        @Option(names = "--activity", description = "New activity level (omit to keep current)")
        private ActivityLevel activityLevel;

        /**
         * Executes the update and prints a confirmation message with the updated values.
         * Prints an error message if the animal is not found.
         */
        @Override
        public void run() {
            try {
                Animal a = AppContext.get().animalApp().updateAnimal(id, name, activityLevel);
                System.out.printf("Updated animal: %s (id=%s)%n", a.getName(), a.getId());
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // remove
    // -------------------------------------------------------------------------

    /**
     * Removes an animal from the system by ID.
     * The animal must not have a pending adoption request.
     */
    @Command(name = "remove", description = "Remove an animal", mixinStandardHelpOptions = true)
    static class RemoveCmd implements Runnable {

        /** The ID of the animal to remove; required. */
        @Option(names = "--id", required = true, description = "Animal ID")
        private String id;

        /**
         * Executes the removal and prints a confirmation message.
         * Prints an error message if the animal is not found or removal is blocked.
         */
        @Override
        public void run() {
            try {
                AppContext.get().animalApp().removeAnimal(id);
                System.out.println("Removed animal: " + id);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}
