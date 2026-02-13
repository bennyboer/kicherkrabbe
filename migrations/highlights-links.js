const sourceCollection = "products_links_lookup";
const targetCollection = "highlights_links_lookup";

const sourceDocs = db.getCollection(sourceCollection).find({}).toArray();

if (sourceDocs.length === 0) {
    print("No documents found in " + sourceCollection);
    quit(0);
}

print("Found " + sourceDocs.length + " documents in " + sourceCollection);

let inserted = 0;
let skipped = 0;

sourceDocs.forEach(function(doc) {
    const existingDoc = db.getCollection(targetCollection).findOne({ _id: doc._id });

    if (existingDoc) {
        skipped++;
        return;
    }

    db.getCollection(targetCollection).insertOne({
        _id: doc._id,
        _class: "de.bennyboer.kicherkrabbe.highlights.persistence.lookup.links.mongo.MongoLookupLink",
        version: doc.version,
        type: doc.type,
        linkId: doc.linkId,
        name: doc.name
    });
    inserted++;
});

print("Migration complete: " + inserted + " inserted, " + skipped + " skipped (already existed)");
