const sourceCollection = "products_permissions";
const targetCollection = "highlights_permissions";

const userPermissions = db.getCollection(sourceCollection).find({
    "holder.type": "USER",
    "action": "CREATE",
    "resource.type": "PRODUCT",
    "resource._id": null
}).toArray();

if (userPermissions.length === 0) {
    print("No user permissions found in " + sourceCollection);
    quit(0);
}

print("Found " + userPermissions.length + " users with CREATE permission on PRODUCT in " + sourceCollection);

let insertedCreate = 0;
let insertedRead = 0;
let skipped = 0;

userPermissions.forEach(function(doc) {
    const userId = doc.holder._id;

    const existingCreatePermission = db.getCollection(targetCollection).findOne({
        "holder._id": userId,
        "holder.type": "USER",
        "action": "CREATE",
        "resource.type": "HIGHLIGHT",
        "resource._id": null
    });

    if (!existingCreatePermission) {
        db.getCollection(targetCollection).insertOne({
            _id: crypto.randomUUID(),
            holder: {
                _id: userId,
                type: "USER"
            },
            action: "CREATE",
            resource: {
                _id: null,
                type: "HIGHLIGHT"
            },
            createdAt: ISODate(),
            _class: "de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermission"
        });
        insertedCreate++;
    } else {
        skipped++;
    }

    const existingReadPermission = db.getCollection(targetCollection).findOne({
        "holder._id": userId,
        "holder.type": "USER",
        "action": "READ",
        "resource.type": "HIGHLIGHT_LINK",
        "resource._id": null
    });

    if (!existingReadPermission) {
        db.getCollection(targetCollection).insertOne({
            _id: crypto.randomUUID(),
            holder: {
                _id: userId,
                type: "USER"
            },
            action: "READ",
            resource: {
                _id: null,
                type: "HIGHLIGHT_LINK"
            },
            createdAt: ISODate(),
            _class: "de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermission"
        });
        insertedRead++;
    } else {
        skipped++;
    }
});

print("Migration complete:");
print("  - CREATE permissions inserted: " + insertedCreate);
print("  - READ permissions inserted: " + insertedRead);
print("  - Skipped (already existed): " + skipped);
