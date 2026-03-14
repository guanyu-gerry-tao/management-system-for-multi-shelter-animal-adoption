# Project Proposal: Multi-Shelter Animal Adoption Management System

We propose to build a Java-based multi-shelter animal adoption management system that models the workflow between shelters and adopters. The system manages animals available for adoption, allows adopters to submit adoption requests, supports transferring animals between shelters, and recommends suitable animals based on adopter preferences and living conditions. The project focuses on a clear object-oriented architecture rather than a complex user interface.

The system is organized into three layers. The domain layer represents the core entities and system state, including Animal (with subclasses such as Dog, Cat, and Rabbit), Shelter, Adopter, AdoptionRequest, and TransferRequest. These classes capture the main relationships among shelters, animals, adopters, and operational requests.

The service layer coordinates system workflows through services such as AdoptionService, TransferService, MatchingService, VaccinationService, and RequestNotificationService.

The strategy layer encapsulates interchangeable rules used by the services, which is where OOD is most clearly reflected. For example, MatchingService can combine multiple matching strategies such as breed preference, activity level, and lifestyle compatibility, while VaccinationService can apply different vaccination strategies depending on animal type, vaccine requirements, or scheduling intervals. This design demonstrates abstraction, composition, and extensibility, since new rules can be added without modifying the core services.

The system’s core features include animal management, multi-shelter coordination, adoption and transfer workflows, vaccination management, recommendation matching, and event notifications.
