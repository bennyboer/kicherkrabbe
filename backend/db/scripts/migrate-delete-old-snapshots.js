/**
 * MongoDB Shell Script: Delete Old SNAPSHOTTED Events
 *
 * This script removes all old SNAPSHOTTED events from the event store.
 * After the migration to automatic reflection-based snapshots, the old
 * SNAPSHOTTED events are no longer needed. Aggregates will rebuild from
 * all events and create new SNAPSHOT events automatically when the
 * threshold is reached.
 *
 * Usage:
 *   mongosh --host localhost:27017 --eval "rs0 = 'rs0'" migrate-delete-old-snapshots.js
 *
 * Or connect to mongosh and run:
 *   load("migrate-delete-old-snapshots.js")
 *
 * IMPORTANT: Run this script AFTER deploying the new code that uses
 * reflection-based snapshots. The new code can still read aggregates
 * without snapshots - it will just rebuild them from all events.
 */

const dbName = "kicherkrabbe";
const db = db.getSiblingDB(dbName);

print("=".repeat(60));
print("Migration: Delete Old SNAPSHOTTED Events");
print("=".repeat(60));
print("");

const collections = db.getCollectionNames().filter(name => name.endsWith("_events"));

print(`Found ${collections.length} event collections to process:`);
collections.forEach(c => print(`  - ${c}`));
print("");

let totalDeleted = 0;

collections.forEach(collectionName => {
    print(`Processing: ${collectionName}`);

    const countBefore = db[collectionName].countDocuments({ name: "SNAPSHOTTED" });

    if (countBefore === 0) {
        print(`  No SNAPSHOTTED events found, skipping.`);
        print("");
        return;
    }

    print(`  Found ${countBefore} SNAPSHOTTED events to delete.`);

    const result = db[collectionName].deleteMany({ name: "SNAPSHOTTED" });

    print(`  Deleted ${result.deletedCount} events.`);
    totalDeleted += result.deletedCount;
    print("");
});

print("=".repeat(60));
print(`Migration complete. Total events deleted: ${totalDeleted}`);
print("=".repeat(60));
print("");
print("Note: Aggregates will now rebuild from all events on first access.");
print("New SNAPSHOT events will be created automatically when the threshold is reached.");
