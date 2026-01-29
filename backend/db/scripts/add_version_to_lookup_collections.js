const collections = [
    "credentials_lookup",
    "users_lookup",
    "products_links_lookup",
    "inquiries_requests"
];

collections.forEach(collectionName => {
    const result = db.getCollection(collectionName).updateMany(
        { version: { $exists: false } },
        { $set: { version: NumberLong(0) } }
    );
    print(`${collectionName}: ${result.modifiedCount} documents updated`);
});
