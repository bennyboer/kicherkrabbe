package de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.mongo;

import de.bennyboer.kicherkrabbe.fabrics.FabricTypeId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.FabricType;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.FabricTypeRepo;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoFabricTypeRepo implements FabricTypeRepo {

    private final String collectionName;

    private final ReactiveMongoTemplate template;

    public MongoFabricTypeRepo(ReactiveMongoTemplate template) {
        this("fabrics_fabric_types", template);
    }

    public MongoFabricTypeRepo(String collectionName, ReactiveMongoTemplate template) {
        this.collectionName = collectionName;
        this.template = template;
    }

    @Override
    public Mono<FabricType> save(FabricType fabricType) {
        return template.save(MongoFabricTypeTransformer.toMongo(fabricType), collectionName)
                .map(MongoFabricTypeTransformer::fromMongo);
    }

    @Override
    public Mono<Void> removeById(FabricTypeId id) {
        Criteria criteria = where("_id").is(id.getValue());
        Query query = query(criteria);

        return template.remove(query, collectionName).then();
    }

    @Override
    public Flux<FabricType> findByIds(Collection<FabricTypeId> ids) {
        Set<String> fabricTypeIds = ids.stream()
                .map(FabricTypeId::getValue)
                .collect(Collectors.toSet());

        Criteria criteria = where("_id").in(fabricTypeIds);
        Query query = query(criteria);

        return template.find(query, MongoFabricType.class, collectionName)
                .map(MongoFabricTypeTransformer::fromMongo);
    }

    @Override
    public Flux<FabricType> findAll() {
        return template.findAll(MongoFabricType.class, collectionName)
                .map(MongoFabricTypeTransformer::fromMongo);
    }

}
