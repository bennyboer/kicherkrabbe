# Messaging Library

RabbitMQ-based messaging infrastructure with transactional outbox/inbox patterns.

## Module Structure

| Module | Purpose |
|--------|---------|
| `messaging-core` | Domain model: outbox entries, inbox messages, routing keys, targets. No Spring dependencies. |
| `messaging-starter` | Spring integration: RabbitMQ listener factory, outbox publisher, MongoDB persistence, configuration. |
| `messaging-testing` | Test support: `@MessagingTest` (in-memory), `@RabbitMessagingTest` (real RabbitMQ), `BaseMessagingTest` helper. |
| `messaging-example` | Integration tests for the library itself. |

## Outbox Pattern

Reliable message publishing through transactional insert + async publish:

1. **Insert** — `MessagingOutbox.insert(entry)` stores entries within an existing transaction
2. **Change stream** — `MessagingOutboxChangeStream` watches for inserts and triggers publishing
3. **Lock & publish** — entries are locked, serialized to JSON, and sent to RabbitMQ with publisher confirms
4. **Acknowledge** — on success, entries are marked acknowledged; on failure, they're unlocked for retry
5. **Scheduled retry** — `MessagingOutboxTasks` retries unpublished entries every 10 seconds, unlocks stale locks, and cleans up old entries

## Inbox Pattern

Idempotent message processing via deduplication:

- Each message is identified by `listener-name + messageId`
- `MessagingInbox.addMessage()` inserts into the inbox within a transaction
- Duplicate messages raise `IncomingMessageAlreadySeenException` and are silently skipped

## Listener Infrastructure

`MessageListenerFactory` creates listeners bound to exchanges with routing key patterns:

- **Durable listeners** — normal queue + dead-letter queue, manual ack, retry with backoff (3 attempts), DLQ on exhaustion
- **Transient listeners** — auto-delete queue, auto-ack, no persistence

### Event Sourcing Integration

`EventListenerFactory` (in `eventsourcing-starter`) wraps `MessageListenerFactory`:

- Parses RabbitMQ messages as domain events with metadata (aggregate ID/type, version, agent, timestamp)
- Supports listening to all events or specific event names via wildcard routing keys

## Testing

### Default: In-Memory (`@MessagingTest`)

Used by all feature-level messaging tests (`EventListenerTest` → `BaseMessagingTest` → `@MessagingTest`).

- No RabbitMQ container required
- `InMemoryMessageBus` routes messages by exchange + routing key pattern matching
- `InMemoryMessageListenerFactory` creates listeners backed by the bus
- `InMemoryOutboxEntryPublisher` serializes to JSON and publishes to the bus
- Full outbox → change stream → publish → listener pipeline exercised

### Opt-In: Real RabbitMQ (`@RabbitMessagingTest`)

For tests that require RabbitMQ-specific behavior (DLQ routing, dead queue reads):

- Starts a RabbitMQ Testcontainer
- Uses `MessagingTestConfig` with `RabbitMessageListenerFactory` and `RabbitOutboxEntryPublisher`

### Contract Tests

`MessageListenerContractTests` verifies behavioral equivalence between both implementations:
basic send/receive, multiple messages, wildcard routing, transient listeners, retry, and resilience.
