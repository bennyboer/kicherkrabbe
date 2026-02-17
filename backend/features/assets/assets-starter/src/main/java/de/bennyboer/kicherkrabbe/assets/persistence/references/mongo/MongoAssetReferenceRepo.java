package de.bennyboer.kicherkrabbe.assets.persistence.references.mongo;

import de.bennyboer.kicherkrabbe.assets.AssetId;
import de.bennyboer.kicherkrabbe.assets.persistence.references.AssetReference;
import de.bennyboer.kicherkrabbe.assets.persistence.references.AssetReferenceRepo;
import de.bennyboer.kicherkrabbe.assets.persistence.references.AssetReferenceResourceType;
import de.bennyboer.kicherkrabbe.assets.persistence.references.AssetResourceId;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoAssetReferenceRepo implements AssetReferenceRepo {

    private final String collectionName;

    private final ReactiveMongoTemplate template;

    public MongoAssetReferenceRepo(ReactiveMongoTemplate template) {
        this("assets_references", template);
    }

    public MongoAssetReferenceRepo(String collectionName, ReactiveMongoTemplate template) {
        this.collectionName = collectionName;
        this.template = template;

        createIndex(new Index().on("assetId", ASC));
        createIndex(new Index().on("resourceType", ASC).on("resourceId", ASC));
    }

    @Override
    public Mono<Void> upsert(AssetReference reference) {
        var doc = MongoAssetReferenceTransformer.toMongo(reference);
        var query = query(where("_id").is(doc.id));
        var update = new Update()
                .set("assetId", doc.assetId)
                .set("resourceType", doc.resourceType)
                .set("resourceId", doc.resourceId);

        return template.upsert(query, update, collectionName).then();
    }

    @Override
    public Mono<Void> removeByResource(AssetReferenceResourceType resourceType, AssetResourceId resourceId) {
        Criteria criteria = where("resourceType").is(resourceType.name())
                .and("resourceId").is(resourceId.getValue());
        Query query = query(criteria);

        return template.remove(query, collectionName).then();
    }

    @Override
    public Flux<AssetReference> findByAssetId(AssetId assetId) {
        Criteria criteria = where("assetId").is(assetId.getValue());
        Query query = query(criteria);

        return template.find(query, MongoAssetReference.class, collectionName)
                .map(MongoAssetReferenceTransformer::fromMongo);
    }

    private void createIndex(Index index) {
        template.indexOps(collectionName)
                .createIndex(index)
                .block();
    }

}
