package shelter.startup;

import shelter.domain.Animal;
import shelter.domain.Shelter;
import shelter.exception.ShelterAtCapacityException;

import java.util.Objects;
import java.util.Optional;

/**
 * Restores in-memory links from loaded animals back to their shelters.
 * CSV stores the animal's shelter ID, so startup needs this step to rebuild
 * each shelter's animal list after repository loading.
 */
public class ShelterAnimalLinker {

    /**
     * Creates a new ShelterAnimalLinker instance.
     * This linker is stateless and restores in-memory shelter-to-animal associations from repository data.
     */
    public ShelterAnimalLinker() {}

    /**
     * Rebuilds shelter-to-animal relationships after CSV data has been loaded.
     *
     * @param repositories the loaded repository bundle
     */
    public void restoreLinks(RepositoryBundle repositories) {
        Objects.requireNonNull(repositories, "Repository bundle must not be null.");

        for (Animal animal : repositories.animalRepository().findAll()) {
            String shelterId = animal.getShelterId();
            if (shelterId == null || shelterId.isBlank()) {
                continue;
            }

            Optional<Shelter> shelter = repositories.shelterRepository().findById(shelterId);
            if (shelter.isEmpty()) {
                warn("Animal " + animal.getId() + " references missing shelter " + shelterId + ".");
                continue;
            }

            try {
                shelter.get().addAnimal(animal);
            } catch (ShelterAtCapacityException e) {
                warn("Could not restore animal " + animal.getId() + " into shelter "
                        + shelterId + " because the shelter is at capacity.");
            } catch (IllegalArgumentException e) {
                warn("Could not restore animal " + animal.getId() + " into shelter "
                        + shelterId + ": " + e.getMessage());
            }
        }
    }

    private void warn(String message) {
        System.err.println("Startup warning: " + message);
    }
}
