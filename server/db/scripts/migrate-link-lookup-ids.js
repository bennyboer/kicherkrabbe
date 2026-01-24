// Migration script to fix LookupLink IDs in MongoDB
// Run with: mongosh <database> migrate-link-lookup-ids.js
// Or: mongosh mongodb://localhost:27017/<database> migrate-link-lookup-ids.js

const collectionName = "products_links_lookup";

print("Starting migration of " + collectionName + " collection...");

const collection = db.getCollection(collectionName);
const allDocs = collection.find({}).toArray();

print("Found " + allDocs.length + " documents to process");

// Group documents by (type, linkId) to handle duplicates
const groupedDocs = {};
allDocs.forEach(doc => {
    const key = doc.type + "-" + doc.linkId;
    if (!groupedDocs[key]) {
        groupedDocs[key] = [];
    }
    groupedDocs[key].push(doc);
});

print("Found " + Object.keys(groupedDocs).length + " unique (type, linkId) combinations");

let migratedCount = 0;
let duplicatesRemoved = 0;
let alreadyCorrectCount = 0;

Object.keys(groupedDocs).forEach(newId => {
    const docs = groupedDocs[newId];

    // Check if already migrated (document with correct ID exists)
    const existingCorrect = docs.find(d => d._id === newId);

    if (existingCorrect) {
        // Already correct, just remove any duplicates
        docs.forEach(doc => {
            if (doc._id !== newId) {
                collection.deleteOne({ _id: doc._id });
                duplicatesRemoved++;
                print("  Removed duplicate: " + doc._id);
            }
        });
        alreadyCorrectCount++;
    } else {
        // Need to migrate - use the first document's data
        const sourceDoc = docs[0];

        // Insert new document with correct ID
        const newDoc = {
            _id: newId,
            type: sourceDoc.type,
            linkId: sourceDoc.linkId,
            name: sourceDoc.name
        };

        try {
            collection.insertOne(newDoc);
            print("  Migrated: " + sourceDoc._id + " -> " + newId);
            migratedCount++;
        } catch (e) {
            print("  ERROR inserting " + newId + ": " + e.message);
        }

        // Remove all old documents
        docs.forEach(doc => {
            collection.deleteOne({ _id: doc._id });
            if (docs.indexOf(doc) > 0) {
                duplicatesRemoved++;
            }
        });
    }
});

print("");
print("Migration complete!");
print("  - Migrated: " + migratedCount);
print("  - Already correct: " + alreadyCorrectCount);
print("  - Duplicates removed: " + duplicatesRemoved);
print("  - Total documents now: " + collection.countDocuments({}));
