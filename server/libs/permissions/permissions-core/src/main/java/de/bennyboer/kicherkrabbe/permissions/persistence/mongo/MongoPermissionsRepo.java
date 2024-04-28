package de.bennyboer.kicherkrabbe.permissions.persistence.mongo;

import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.serializer.MongoPermissionSerializer;
import org.bson.Document;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoPermissionsRepo implements PermissionsRepo {

    private final String collectionName;

    private final ReactiveMongoTemplate template;

    public MongoPermissionsRepo(String collectionName, ReactiveMongoTemplate template) {
        this.collectionName = collectionName;
        this.template = template;

        initializeIndices(template.indexOps(collectionName)).block();
    }

    @Override
    public Mono<Permission> insert(Permission permission) {
        return insertAll(List.of(permission)).next();
    }

    @Override
    public Flux<Permission> insertAll(Collection<Permission> permissions) {
        List<MongoPermission> mongoPermissions = permissions.stream()
                .map(MongoPermissionSerializer::serialize)
                .toList();

        return template.insert(mongoPermissions, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Mono<Boolean> hasPermission(Permission permission) {
        MongoPermission mongoPermission = MongoPermissionSerializer.serialize(permission);

        Criteria criteria = where("action").is(mongoPermission.action)
                .and("holder.type").is(mongoPermission.holder.type)
                .and("holder.id").is(mongoPermission.holder.id)
                .and("resource.type").is(mongoPermission.resource.type)
                .and("resource.id").is(mongoPermission.resource.id);

        return template.exists(query(criteria), collectionName);
    }

    @Override
    public Flux<Permission> findPermissionsByHolder(Holder holder) {
        Criteria criteria = where("holder.type").is(holder.getType())
                .and("holder.id").is(holder.getId().getValue());

        return template.find(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Flux<Permission> findPermissionsByHolderAndResourceType(Holder holder, ResourceType resourceType) {
        Criteria criteria = where("holder.type").is(holder.getType())
                .and("holder.id").is(holder.getId().getValue())
                .and("resource.type").is(resourceType.getName());

        return template.find(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Flux<Permission> findPermissionsByHolderAndResourceTypeAndAction(
            Holder holder,
            ResourceType resourceType,
            Action action
    ) {
        Criteria criteria = where("holder.type").is(holder.getType())
                .and("holder.id").is(holder.getId().getValue())
                .and("resource.type").is(resourceType.getName())
                .and("action").is(action.getName());

        return template.find(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Flux<Permission> findPermissionsByHolderAndResource(Holder holder, Resource resource) {
        Criteria criteria = where("holder.type").is(holder.getType())
                .and("holder.id").is(holder.getId().getValue())
                .and("resource.type").is(resource.getType().getName())
                .and("resource.id").is(resource.getId().map(ResourceId::getValue).orElse(null));

        return template.find(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Flux<Permission> removeByHolder(Holder holder) {
        Criteria criteria = where("holder.type").is(holder.getType())
                .and("holder.id").is(holder.getId().getValue());

        return template.findAllAndRemove(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Flux<Permission> removeByResource(Resource resource) {
        Criteria criteria = where("resource.type").is(resource.getType().getName())
                .and("resource.id").is(resource.getId().map(ResourceId::getValue).orElse(null));

        return template.findAllAndRemove(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Flux<Permission> removeByHolderAndResource(Holder holder, Resource resource) {
        Criteria criteria = where("holder.type").is(holder.getType())
                .and("holder.id").is(holder.getId().getValue())
                .and("resource.type").is(resource.getType().getName())
                .and("resource.id").is(resource.getId().map(ResourceId::getValue).orElse(null));

        return template.findAllAndRemove(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Mono<Permission> removeByPermission(Permission permission) {
        MongoPermission mongoPermission = MongoPermissionSerializer.serialize(permission);

        Criteria criteria = where("action").is(mongoPermission.action)
                .and("holder.type").is(mongoPermission.holder.type)
                .and("holder.id").is(mongoPermission.holder.id)
                .and("resource.type").is(mongoPermission.resource.type)
                .and("resource.id").is(mongoPermission.resource.id);

        return template.findAndRemove(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    private Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        IndexDefinition permissionsIndex = new CompoundIndexDefinition(new Document()
                .append("action", 1)
                .append("holder.type", 1)
                .append("holder.id", 1)
                .append("resource.type", 1)
                .append("resource.id", 1));

        IndexDefinition holderIndex = new CompoundIndexDefinition(new Document()
                .append("holder.type", 1)
                .append("holder.id", 1));

        IndexDefinition resourceIndex = new CompoundIndexDefinition(new Document()
                .append("resource.type", 1)
                .append("resource.id", 1));

        IndexDefinition actionIndex = new Index().on("action", ASC);

        return Mono.zip(
                indexOps.ensureIndex(permissionsIndex),
                indexOps.ensureIndex(holderIndex),
                indexOps.ensureIndex(resourceIndex),
                indexOps.ensureIndex(actionIndex)
        ).then();
    }

}
