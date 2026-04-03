# Use Cases

This document describes the use cases for the Multi-Shelter Animal Adoption Management System.

**Note on actors**: This is a demo project. The system uses a single hardcoded admin staff member with all permissions. In a real application, different roles would have different access.

**Note on CLI**: All use cases are executed via the `shelter` CLI tool. Each use case corresponds to one method in the Application Layer, which orchestrates the underlying Service calls.

**Note on adoption state**:
- `Animal.adopterId` — `null` means available; a non-null adopter ID means the animal has been adopted.
- `Adopter.adoptedAnimalIds` — a list of all animal IDs this adopter has ever adopted.
- Both fields are updated together when an adoption request is approved.

---

## UC-00: System Startup

**Actor**: Staff

The system loads all persisted data from `~/shelter/data/` on startup. If no data files exist, the system initializes with empty state. A hardcoded admin staff member is set as the current operator for the session.

**Application Layer**: `SystemStartup.initialize()`

**Services involved**: All services (load from CSV)

---

## UC-01: Shelter Management

### UC-01.1: Register a New Shelter

**Actor**: Staff

Staff registers a new shelter with a name, location, and capacity. The system persists the shelter record.

**Application Layer**: `ShelterApplicationService.registerShelter(name, location, capacity)`

**Services involved**:
- `ShelterService.register(shelter)`
- `AuditService.log("registered shelter", shelter)`

**Error cases**:
- Shelter with same name and location already exists → exception

---

### UC-01.2: View All Shelters

**Actor**: Staff

Staff retrieves a list of all registered shelters, including their IDs, names, locations, and current capacity usage.

**Application Layer**: `ShelterApplicationService.listShelters()`

**Services involved**:
- `ShelterService.listAll()`

**Error cases**:
- No shelters registered → print message indicating empty list

---

### UC-01.3: Update Shelter Information

**Actor**: Staff

Staff updates an existing shelter's details. Only the fields provided via CLI are changed; omitted fields retain their current values. The CLI layer fetches the current shelter, merges the provided fields, then passes the complete object to the application layer.

**Application Layer**: `ShelterApplicationService.updateShelter(shelterId, name, location, capacity)`

**Services involved**:
- `ShelterService.update(shelter)`
- `AuditService.log("updated shelter", shelter)`

**Error cases**:
- Shelter not found → exception

---

### UC-01.4: Remove a Shelter

**Actor**: Staff

Staff removes a shelter from the system. The shelter must have no animals and no pending transfer requests.

**Application Layer**: `ShelterApplicationService.removeShelter(shelterId)`

**Services involved**:
- `ShelterService.remove(shelter)`
- `AuditService.log("removed shelter", shelter)`

**Error cases**:
- Shelter not found → exception
- Shelter still holds animals → exception
- Shelter has pending transfer requests → exception

---

## UC-02: Animal Management

### UC-02.1: Admit a New Animal

**Actor**: Staff

Staff admits a new animal into a shelter, providing species, name, breed, age, and activity level. The animal is initialized with no adopter and is immediately available for matching.

**Note**: Use UC-01.2 first to obtain a valid shelter ID.

**Application Layer**: `AnimalApplicationService.admitAnimal(species, name, breed, age, activityLevel, shelterId)`

**Services involved**:
- `ShelterService` (look up shelter)
- `AnimalService.register(animal, shelter)`
- `AuditService.log("admitted", animal)`

**Error cases**:
- Shelter not found → exception
- Shelter at capacity → exception

---

### UC-02.2: View Animals

**Actor**: Staff

Staff retrieves a list of animals. Without a shelter filter, all animals in the system are returned. With `--shelter`, only animals in that shelter are returned.

**Application Layer**: `AnimalApplicationService.listAnimals(shelterId)` — `shelterId` is optional; `null` returns all animals system-wide.

**Services involved**:
- `AnimalService.getAnimalsByShelter(shelter)` (if shelter specified)
- `AnimalService` list all (if no shelter specified)

**Error cases**:
- Shelter not found (if specified) → exception

---

### UC-02.3: Update Animal Information

**Actor**: Staff

Staff updates an existing animal's details. Only the fields provided via CLI are changed; omitted fields retain their current values. The CLI layer fetches the current animal, merges the provided fields, then passes the complete object to the application layer.

**Application Layer**: `AnimalApplicationService.updateAnimal(animalId, name, breed, age, activityLevel)`

**Services involved**:
- `AnimalService.update(animal)`
- `AuditService.log("updated", animal)`

**Error cases**:
- Animal not found → exception

---

### UC-02.4: Remove an Animal

**Actor**: Staff

Staff removes an animal from the system. The animal must not have a pending adoption request.

**Application Layer**: `AnimalApplicationService.removeAnimal(animalId)`

**Services involved**:
- `AnimalService.remove(animal)`
- `AuditService.log("removed", animal)`

**Error cases**:
- Animal not found → exception
- Animal has a pending adoption request → exception

---

## UC-03: Adopter Management

### UC-03.1: Register a New Adopter

**Actor**: Staff

Staff registers a new adopter. All fields are passed as individual arguments. The application layer constructs the `Adopter` and `AdopterPreferences` objects internally. The adopter is initialized with an empty adopted animals list.

**Application Layer**: `AdopterApplicationService.registerAdopter(name, livingSpace, dailySchedule, preferredSpecies, preferredBreed, preferredActivityLevel, minAge, maxAge)`

**Services involved**:
- `AdopterService.register(adopter)`
- `AuditService.log("registered adopter", adopter)`

**Error cases**:
- Adopter already registered → exception
- Required fields missing → exception

---

### UC-03.2: View All Adopters

**Actor**: Staff

Staff retrieves a list of all registered adopters and their IDs. No shelter filter — adopters are not scoped to a shelter.

**Application Layer**: `AdopterApplicationService.listAdopters()`

**Services involved**:
- `AdopterService.listAll()`

---

### UC-03.3: Update Adopter Information

**Actor**: Staff

Staff updates an existing adopter's personal details or preferences. Only the fields provided via CLI are changed; omitted fields retain their current values. The CLI layer fetches the current adopter, merges the provided fields, then passes the complete object to the application layer.

**Application Layer**: `AdopterApplicationService.updateAdopter(adopterId, name, livingSpace, dailySchedule, preferredSpecies, preferredBreed, preferredActivityLevel, minAge, maxAge)`

**Services involved**:
- `AdopterService.update(adopter)`
- `AuditService.log("updated adopter", adopter)`

**Error cases**:
- Adopter not found → exception

---

### UC-03.4: Remove an Adopter

**Actor**: Staff

Staff removes an adopter from the system. The adopter must not have a pending adoption request.

**Application Layer**: `AdopterApplicationService.removeAdopter(adopterId)`

**Services involved**:
- `AdopterService.remove(adopter)`
- `AuditService.log("removed adopter", adopter)`

**Error cases**:
- Adopter not found → exception
- Adopter has a pending adoption request → exception

---

## UC-04: Matching

### UC-04.1: Match Animals for an Adopter

**Actor**: Staff

Staff finds the best-matching available animals for a given adopter. The system scores all animals whose `adopterId` is null in the specified shelter and returns results ranked by score.

**Note**: Use UC-01.2 and UC-03.2 first to obtain valid IDs.

**Application Layer**: `MatchingApplicationService.matchAnimalsForAdopter(adopterId, shelterId, withExplanation)`

**Services involved**:
- `AdopterService` (look up adopter)
- `AnimalService.getAnimalsByShelter(shelter)` filtered to `animal.isAvailable()`
- `AdopterBasedMatchingService.match(adopter, animals)`
- `ExplanationService.explain(results)` (if `withExplanation` is true)

**Error cases**:
- Adopter not found → exception
- Shelter not found → exception
- No available animals → returns empty list

---

### UC-04.2: Match Adopters for an Animal

**Actor**: Staff

Staff finds the best-matching adopters for a given animal. The system scores all registered adopters and returns results ranked by score.

**Note**: Use UC-02.2 and UC-03.2 first to obtain valid IDs.

**Application Layer**: `MatchingApplicationService.matchAdoptersForAnimal(animalId, withExplanation)`

**Services involved**:
- `AnimalService` (look up animal, verify `isAvailable()`)
- `AdopterService.listAll()`
- `AnimalBasedMatchingService.match(animal, adopters)`
- `ExplanationService.explain(results)` (if `withExplanation` is true)

**Error cases**:
- Animal not found → exception
- Animal already adopted → exception
- No adopters registered → returns empty list

---

## UC-05: Adoption Workflow

### UC-05.1: Submit an Adoption Request

**Actor**: Staff

Staff submits an adoption request on behalf of an adopter for a specific animal. The animal must be available (`adopterId == null`).

**Note**: Use UC-02.2 and UC-03.2 first to obtain valid IDs.

**Application Layer**: `AdoptionApplicationService.submitRequest(adopterId, animalId)`

**Services involved**:
- `AdopterService` (look up adopter)
- `AnimalService` (look up animal, verify `isAvailable()`)
- `AdoptionService.submit(request)`
- `AuditService.log("submitted adoption request", request)`

**Error cases**:
- Adopter or animal not found → exception
- Animal not available (`adopterId != null`) → exception

---

### UC-05.2: Approve an Adoption Request

**Actor**: Staff

Staff approves a pending adoption request. The system sets `animal.adopterId` to the adopter's ID, adds the animal ID to `adopter.adoptedAnimalIds`, and notifies the adopter.

**Application Layer**: `AdoptionApplicationService.approveRequest(requestId)`

**Services involved**:
- `AdoptionService.approve(request)`
- `animal.setAdopterId(adopterId)` and `adopter.addAdoptedAnimalId(animalId)`
- `RequestNotificationService.notifyAdoptionStatusChange(request)`
- `AuditService.log("approved adoption request", request)`

**Error cases**:
- Request not found → exception
- Request not in PENDING state → exception

---

### UC-05.3: Reject an Adoption Request

**Actor**: Staff

Staff rejects a pending adoption request. The animal remains available.

**Application Layer**: `AdoptionApplicationService.rejectRequest(requestId)`

**Services involved**:
- `AdoptionService.reject(request)`
- `RequestNotificationService.notifyAdoptionStatusChange(request)`
- `AuditService.log("rejected adoption request", request)`

**Error cases**:
- Request not found → exception
- Request not in PENDING state → exception

---

### UC-05.4: Cancel an Adoption Request

**Actor**: Staff

Staff cancels a pending adoption request before it is reviewed. The animal remains available for other requests.

**Application Layer**: `AdoptionApplicationService.cancelRequest(requestId)`

**Services involved**:
- `AdoptionService.cancel(request)`
- `RequestNotificationService.notifyAdoptionStatusChange(request)`
- `AuditService.log("cancelled adoption request", request)`

**Error cases**:
- Request not found → exception
- Request not in PENDING state → exception

---

## UC-06: Transfer Workflow

### UC-06.1: Request an Animal Transfer

**Actor**: Staff

Staff initiates a transfer request to move an animal from one shelter to another. The animal must be available.

**Note**: Use UC-01.2 and UC-02.2 first to obtain valid IDs.

**Application Layer**: `TransferApplicationService.requestTransfer(animalId, fromShelterId, toShelterId)`

**Services involved**:
- `AnimalService` (look up animal, verify `isAvailable()`)
- `ShelterService` (look up shelters)
- `TransferService.requestTransfer(animal, from, to)`
- `RequestNotificationService.notifyTransferStatusChange(request)`
- `AuditService.log("requested transfer", request)`

**Error cases**:
- Animal not in source shelter → exception
- Animal not available → exception
- Destination shelter at capacity → exception

---

### UC-06.2: Approve a Transfer Request

**Actor**: Staff

Staff approves a pending transfer request. The animal is moved to the destination shelter and records are updated.

**Application Layer**: `TransferApplicationService.approveTransfer(requestId)`

**Services involved**:
- `TransferService.approve(request)`
- `RequestNotificationService.notifyTransferStatusChange(request)`
- `AuditService.log("approved transfer", request)`

**Error cases**:
- Request not in PENDING state → exception

---

### UC-06.3: Reject a Transfer Request

**Actor**: Staff

Staff rejects a pending transfer request. The animal remains in the source shelter.

**Application Layer**: `TransferApplicationService.rejectTransfer(requestId)`

**Services involved**:
- `TransferService.reject(request)`
- `RequestNotificationService.notifyTransferStatusChange(request)`
- `AuditService.log("rejected transfer", request)`

**Error cases**:
- Request not in PENDING state → exception

---

### UC-06.4: Cancel a Transfer Request

**Actor**: Staff

Staff cancels a pending transfer request that they initiated. The animal remains in the source shelter and stays available.

**Application Layer**: `TransferApplicationService.cancelTransfer(requestId)`

**Services involved**:
- `TransferService.cancel(request)`
- `AuditService.log("cancelled transfer", request)`

**Error cases**:
- Request not found → exception
- Request not in PENDING state → exception

---

## UC-07: Vaccination Management

### UC-07.1: Record a Vaccination

**Actor**: Staff

Staff records that an animal received a specific vaccine on a given date.

**Note**: Use UC-02.2 first to obtain a valid animal ID.

**Application Layer**: `VaccinationApplicationService.recordVaccination(animalId, vaccineTypeName, date)`

**Services involved**:
- `AnimalService` (look up animal)
- `VaccineTypeCatalogService.findByName(name)`
- `VaccinationService.recordVaccination(animal, vaccineType, date)`
- `AuditService.log("recorded vaccination", animal)`

**Error cases**:
- Animal not found → exception
- Vaccine type not found in catalog → exception
- Vaccine not applicable to animal's species → exception

---

### UC-07.2: Check Overdue Vaccinations

**Actor**: Staff

Staff checks which vaccinations are overdue for a given animal.

**Application Layer**: `VaccinationApplicationService.getOverdueVaccinations(animalId)`

**Services involved**:
- `AnimalService` (look up animal)
- `VaccinationService.getOverdueVaccinations(animal)`

**Error cases**:
- Animal not found → exception

---

### UC-07.3: Add a Vaccine Type

**Actor**: Staff

Staff adds a new vaccine type to the catalog, specifying the name, applicable species, and validity period in days.

**Application Layer**: `VaccinationApplicationService.addVaccineType(name, applicableSpecies, validityDays)`

**Services involved**:
- `VaccineTypeCatalogService.add(vaccineType)`
- `AuditService.log("added vaccine type", vaccineType)`

**Error cases**:
- Duplicate vaccine type name → exception

---

### UC-07.4: Update a Vaccine Type

**Actor**: Staff

Staff updates an existing vaccine type's details by ID. Only the fields provided via CLI are changed; omitted fields retain their current values. Name is also updatable since ID is the unique identifier.

**Note**: Use UC-07.6 first to obtain a valid vaccine type ID.

**Application Layer**: `VaccinationApplicationService.updateVaccineType(id, name, applicableSpecies, validityDays)`

**Services involved**:
- `VaccineTypeCatalogService.update(vaccineType)`
- `AuditService.log("updated vaccine type", vaccineType)`

**Error cases**:
- Vaccine type not found → exception
- Duplicate name after update → exception

---

### UC-07.5: Remove a Vaccine Type

**Actor**: Staff

Staff removes a vaccine type from the catalog by ID.

**Note**: Use UC-07.6 first to obtain a valid vaccine type ID.

**Application Layer**: `VaccinationApplicationService.removeVaccineType(id)`

**Services involved**:
- `VaccineTypeCatalogService.remove(id)`
- `AuditService.log("removed vaccine type", id)`

**Error cases**:
- Vaccine type not found → exception

---

### UC-07.6: View All Vaccine Types

**Actor**: Staff

Staff retrieves a list of all vaccine types in the catalog, including their IDs, names, applicable species, and validity periods.

**Application Layer**: `VaccinationApplicationService.listVaccineTypes()`

**Services involved**:
- `VaccineTypeCatalogService.listAll()`

---

## UC-08: Audit Log

### UC-08.1: View Audit Log

**Actor**: Staff

Staff views the full audit log of all actions performed in the current session.

**Application Layer**: `AuditApplicationService.getLog()`

**Services involved**:
- `AuditService.getLog()`
