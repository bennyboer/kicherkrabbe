// Populate offers_categories from existing CLOTHING categories in categories_lookup.
// Run with: mongosh <connection-string> populate-offers-categories.mongosh.js

const clothingCategories = db.categories_lookup.find({ group: "CLOTHING" }).toArray();

if (clothingCategories.length === 0) {
    print("No CLOTHING categories found in categories_lookup. Nothing to migrate.");
} else {
    const ops = clothingCategories.map(c => ({
        updateOne: {
            filter: { _id: c._id },
            update: { $set: { _id: c._id, name: c.name } },
            upsert: true
        }
    }));

    const result = db.offers_categories.bulkWrite(ops);
    print(`Migration complete. Upserted: ${result.upsertedCount}, Modified: ${result.modifiedCount}, Matched: ${result.matchedCount}`);
}
