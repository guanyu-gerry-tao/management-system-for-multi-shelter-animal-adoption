# Multi-Shelter Animal Adoption Management System

A Java-based system for managing animal adoption workflows across multiple shelters. Built as a final project for CS 5004 at Northeastern University, with a focus on object-oriented design principles.

## Overview

The system models the real-world workflow between animal shelters and prospective adopters. It supports animal management, adoption and transfer requests, vaccination tracking, smart animal-adopter matching via configurable strategies, and AI-assisted match explanations.

The project prioritizes a clean OOD architecture over a complex UI.

## Architecture

The system is organized into three layers:

```
Strategy Layer  ←  interchangeable rules (MatchingStrategy, VaccinationStrategy, ...)
Service Layer   ←  business workflows (AdoptionService, MatchingService, ...)
Domain Layer    ←  core entities (Animal, Shelter, Adopter, AdoptionRequest, ...)
```

### Domain Layer

| Class | Description |
|---|---|
| `Animal` | Base class with subclasses `Dog`, `Cat`, `Rabbit` |
| `Shelter` | Holds a collection of animals, manages capacity and location |
| `Adopter` | Prospective adopter with preferences and lifestyle context |
| `AdoptionRequest` | Links an adopter to a target animal, tracks request status |
| `TransferRequest` | Models an inter-shelter animal transfer request |

### Service Layer

| Service | Description |
|---|---|
| `AdoptionService` | Manages adoption request lifecycle (submit, validate, approve, reject) |
| `TransferService` | Handles inter-shelter transfers |
| `MatchingService` | Scores and ranks animals for a given adopter using configurable strategies |
| `VaccinationService` | Tracks vaccination records and schedules reminders |
| `RequestNotificationService` | Dispatches status updates to adopters and shelter staff |
| `ExplanationService` | Interface with `AIExplanationService` (external AI API) and `MockExplanationService` (for testing) |

### Strategy Layer

Encapsulates interchangeable rules consumed by the services:

- **MatchingStrategy** → `BreedStrategy`, `ActivityStrategy`, `LifestyleStrategy`
- New strategies can be added without modifying core services (Open/Closed Principle)

## Key Features

- Multi-shelter animal and capacity management
- Adoption and inter-shelter transfer request workflows
- Vaccination record tracking and follow-up reminders
- Adopter-animal matching scored by composable strategy rules
- AI-assisted natural-language explanation of match results (post-processing only, does not affect scores)
- Event notifications on request status changes

## Getting Started

**Prerequisites**: Java 21+, Maven

```bash
git clone https://github.com/guanyu-gerry-tao/management-system-for-multi-shelter-animal-adoption.git
cd management-system-for-multi-shelter-animal-adoption
mvn compile
mvn test
```

## Project Structure

```
src/
  main/java/
    domain/       # Animal, Shelter, Adopter, AdoptionRequest, TransferRequest
    service/      # AdoptionService, MatchingService, TransferService, ...
    strategy/     # MatchingStrategy and implementations
  test/java/      # JUnit unit tests
docs/
  proposal.md           # Machine-readable proposal with diagrams
  proposal-for-pdf.md   # Human-readable proposal
  diagram-class.mmd     # Class diagram (Mermaid source)
  diagram-layer.mmd     # Layer architecture diagram
```

## Team

| Name | GitHub | Email | Student ID |
|---|---|---|---|
| Guanyu (Gerry) Tao | [@guanyu-gerry-tao](https://github.com/guanyu-gerry-tao) | tao.gua@northeastern.edu | 002593169 |
| Yiying (Irene) Xie | [@Bestpart-Irene](https://github.com/Bestpart-Irene) | xie.yiyi@northeastern.edu | 002591741 |
| Yuxi (Molly) Ou | [@mollyouyuxi](https://github.com/mollyouyuxi) | ou.yux@northeastern.edu | 003163051 |