package shelter.startup;

import shelter.repository.csv.CsvAdopterRepository;
import shelter.repository.csv.CsvAdoptionRequestRepository;
import shelter.repository.csv.CsvAnimalRepository;
import shelter.repository.csv.CsvAuditRepository;
import shelter.repository.csv.CsvShelterRepository;
import shelter.repository.csv.CsvTransferRequestRepository;
import shelter.repository.csv.CsvVaccinationRecordRepository;
import shelter.repository.csv.CsvVaccineTypeRepository;

import java.nio.file.Path;

/**
 * Creates all CSV-backed repositories needed by the application.
 * Repository construction is also the data-loading step because each CSV repository
 * initializes and loads its file in the constructor.
 */
public class CsvRepositoryFactory {

    /**
     * Creates all repositories backed by the given data directory.
     *
     * @param dataDir the directory containing the CSV data files
     * @return the created repository bundle
     */
    public RepositoryBundle create(Path dataDir) {
        if (dataDir == null) {
            throw new IllegalArgumentException("Data directory must not be null.");
        }

        String dataDirPath = dataDir.toString();

        CsvShelterRepository shelterRepository = new CsvShelterRepository(dataDirPath);
        CsvAnimalRepository animalRepository = new CsvAnimalRepository(dataDirPath);
        CsvAdopterRepository adopterRepository = new CsvAdopterRepository(dataDirPath);
        CsvAdoptionRequestRepository adoptionRequestRepository =
                new CsvAdoptionRequestRepository(dataDirPath, animalRepository, adopterRepository);
        CsvTransferRequestRepository transferRequestRepository =
                new CsvTransferRequestRepository(dataDirPath, animalRepository, shelterRepository);
        CsvVaccineTypeRepository vaccineTypeRepository = new CsvVaccineTypeRepository(dataDirPath);
        CsvVaccinationRecordRepository vaccinationRecordRepository =
                new CsvVaccinationRecordRepository(dataDirPath, animalRepository);
        CsvAuditRepository auditRepository = new CsvAuditRepository(dataDirPath);

        return new RepositoryBundle(
                shelterRepository,
                animalRepository,
                adopterRepository,
                adoptionRequestRepository,
                transferRequestRepository,
                vaccineTypeRepository,
                vaccinationRecordRepository,
                auditRepository);
    }
}
