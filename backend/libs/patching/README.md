# Database Patching

Automated MongoDB schema patching system that runs at application startup with distributed locking.

## Problem

Database migrations were applied manually via `mongosh` over SSH. This doesn't scale with multiple service instances and is error-prone.

## Approach

Patches are versioned Java classes that implement `DatabasePatch`. At startup, a `PatchingEngine` acquires a distributed lock in MongoDB, applies all pending patches in version order, bumps the stored version after each patch, and releases the lock. Other instances wait for the lock holder to finish.

If a patch fails, the application crashes on startup (fail-fast). The version in the database reflects the last successfully applied patch, so a redeployment will retry from where it left off.

## Usage

Define patches as Spring beans:

```java
@Bean
DatabasePatch addEmailIndexPatch() {
    return new DatabasePatch() {
        @Override
        public int getVersion() {
            return 1;
        }

        @Override
        public Mono<Void> apply(ReactiveMongoTemplate template) {
            return template.indexOps("users")
                    .createIndex(new Index().on("email", Sort.Direction.ASC).unique())
                    .then();
        }
    };
}
```

Patches are auto-collected and executed at startup via Spring auto-configuration.

## Rules

- Versions must be `>= 1` and strictly increasing
- Patches must be idempotent (a crash after apply but before version bump will re-run the patch)
- Never modify or reorder already-deployed patches
- A failed patch crashes the application

## Distributed Locking

Multiple instances can start concurrently. Only one runs patches:

1. **Acquire**: Atomic `findAndModify` with filter `{ lockedBy: null OR lockedAt < now - 5min }`, upsert on first run
2. **Patch**: Apply pending patches sequentially, bump version after each
3. **Release**: Set `lockedBy` and `lockedAt` to null
4. **Wait**: Other instances poll every 2s (up to 5min) until the lock holder finishes

The 5-minute lock timeout handles crashed instances.

## Module Structure

| Module | Purpose |
|--------|---------|
| `patching-core` | `DatabasePatch` interface (no Spring dependency beyond MongoDB) |
| `patching-starter` | Engine, locking, Spring auto-configuration |
| `patching-example` | Tests |

## Verification

```bash
./gradlew :libs:patching:patching-example:test
```
