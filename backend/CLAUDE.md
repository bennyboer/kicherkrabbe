# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

```bash
# Build the server JAR (requires frontend to be built first)
./gradlew :apps:api:bootJar

# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :features:fabrics:fabrics-example:test

# Run a single test class
./gradlew :features:fabrics:fabrics-example:test --tests "de.bennyboer.kicherkrabbe.fabrics.CreateFabricTest"

# Run a single test method
./gradlew :features:fabrics:fabrics-example:test --tests "de.bennyboer.kicherkrabbe.fabrics.CreateFabricTest.shouldCreateFabricAsUser"

# Clean build
./gradlew clean build

# Start local infrastructure (MongoDB + RabbitMQ)
docker-compose up
```

## Prerequisites

- Java 25
- Docker (for tests and local infrastructure)
- MongoDB (localhost:27017 with replica set `rs0`)
- RabbitMQ (localhost:5672)

## Architecture Overview

This is a **Spring Boot WebFlux** application using **reactive programming** (Project Reactor), **event sourcing**, and **CQRS** patterns.

### Module Structure

Each feature follows a three-module pattern:

- **`feature-core`**: Domain model with aggregates, commands, events, and value objects. No Spring dependencies.
- **`feature-starter`**: Spring integration layer with HTTP handlers, messaging, persistence, and module configuration.
- **`feature-example`**: Tests only. Contains integration tests for the module.

### Key Libraries (`libs/`)

- **`eventsourcing`**: Event sourcing infrastructure. Aggregates implement `Aggregate` interface with `apply(Command)` returning events and `apply(Event)` for state reconstruction.
- **`messaging`**: RabbitMQ-based event publishing and listening via `EventListenerFactory`.
- **`permissions`**: Permission system for resource-based access control.
- **`persistence`**: MongoDB persistence support.
- **`changes`**: Resource change tracking for real-time updates.

### Event Sourcing Pattern

Aggregates are immutable and rebuilt from events. Example pattern from `Fabric`:

```java
public ApplyCommandResult apply(Command cmd, Agent agent) {
    return switch (cmd) {
        case CreateCmd c -> ApplyCommandResult.of(CreatedEvent.of(...));
        case DeleteCmd ignored -> ApplyCommandResult.of(DeletedEvent.of());
        // ...
    };
}

public Aggregate apply(Event event, EventMetadata metadata) {
    return (switch (event) {
        case CreatedEvent e -> withName(e.getName())...;
        case DeletedEvent ignored -> withDeletedAt(metadata.getDate());
        // ...
    }).withVersion(metadata.getAggregateVersion());
}
```

### Module Layer Pattern

Each feature has a `XxxModule` class that orchestrates:
- Business operations with permission checks
- Lookup repository updates
- Permission management

The `XxxMessaging` class defines event listeners that react to domain events (own and from other modules) to maintain consistency.

### HTTP Layer

Uses Spring WebFlux functional endpoints:
- `XxxHttpHandler`: Request handling, transaction management, error mapping
- `XxxHttpConfig`: Route definitions and security customizations

### Testing Pattern

Module tests extend a base test class (e.g., `FabricsModuleTest`) that:
- Sets up in-memory repositories
- Provides helper methods that simulate the full flow (including lookup updates and permission grants)
- Tests are self-contained BDD-style scenarios

### Data Flow

1. HTTP request → `HttpHandler` extracts agent from principal
2. `Module` checks permissions, validates input, calls domain service
3. Domain service loads aggregate from event store, applies command, persists new events
4. Event listeners react to published events (update lookups, grant/revoke permissions, sync related modules)

## Key Conventions

- Use `Preconditions.notNull()` and `Preconditions.check()` for validation
- Value objects use Lombok `@Value` with private constructor via `@AllArgsConstructor(access = PRIVATE)`
- All async operations return `Mono<T>` or `Flux<T>`
- Transactions are managed at HTTP handler level using `TransactionalOperator`
- Module methods that modify state require `@Transactional(propagation = MANDATORY)`
