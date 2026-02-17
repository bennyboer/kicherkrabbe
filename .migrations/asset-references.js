db = db.getSiblingDB("kicherkrabbe");

var references = [];

db.fabrics_lookup.find({imageId: {$exists: true, $ne: null}}).forEach(function (doc) {
    references.push({
        _id: doc.imageId + "_FABRIC_" + doc._id,
        assetId: doc.imageId,
        resourceType: "FABRIC",
        resourceId: doc._id
    });
});

db.patterns_lookup.find({images: {$exists: true}}).forEach(function (doc) {
    (doc.images || []).forEach(function (imageId) {
        references.push({
            _id: imageId + "_PATTERN_" + doc._id,
            assetId: imageId,
            resourceType: "PATTERN",
            resourceId: doc._id
        });
    });
});

db.products_product_lookup.find({images: {$exists: true}}).forEach(function (doc) {
    (doc.images || []).forEach(function (imageId) {
        references.push({
            _id: imageId + "_PRODUCT_" + doc._id,
            assetId: imageId,
            resourceType: "PRODUCT",
            resourceId: doc._id
        });
    });
});

db.highlights_lookup.find({imageId: {$exists: true, $ne: null}}).forEach(function (doc) {
    references.push({
        _id: doc.imageId + "_HIGHLIGHT_" + doc._id,
        assetId: doc.imageId,
        resourceType: "HIGHLIGHT",
        resourceId: doc._id
    });
});

if (references.length > 0) {
    db.assets_references.insertMany(references, {ordered: false});
    print("Inserted " + references.length + " asset references.");
} else {
    print("No asset references to insert.");
}
