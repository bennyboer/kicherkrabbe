package de.bennyboer.kicherkrabbe.permissions.persistence.mongo;

import com.mongodb.MongoBulkWriteException;
import de.bennyboer.kicherkrabbe.permissions.*;
import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.serializer.MongoHolderTypeSerializer;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.serializer.MongoPermissionSerializer;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.mongodb.core.BulkOperations.BulkMode.UNORDERED;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Slf4j
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
        return insert(List.of(permission)).next();
    }

    @Override
    public Flux<Permission> insert(Collection<Permission> permissions) {
        /*
        Normally calling bulkInsert would suffice, but strangely the transaction seems to be
        aborted by mongodb if there is a duplicate permission we want to insert already in the collection.
        For now we just filter out the permissions that are already in the collection beforehand.
        That is not ideal because slower, but works for now..
         */
        return Flux.fromIterable(permissions)
                .filterWhen(p -> hasPermission(p).map(has -> !has))
                .collectList()
                .filter(list -> !list.isEmpty())
                .flatMapMany(this::bulkInsert);
    }

    @Override
    public Mono<Boolean> hasPermission(Permission permission) {
        String actionName = permission.getAction().getName();
        MongoHolderType holderType = MongoHolderTypeSerializer.serialize(permission.getHolder().getType());
        String holderId = permission.getHolder().getId().getValue();
        String resourceTypeName = permission.getResource()
                .getType()
                .getName();
        String resourceId = permission.getResource()
                .getId()
                .map(ResourceId::getValue)
                .orElse(null);

        Criteria criteria = where("action").is(actionName)
                .and("holder.type").is(holderType)
                .and("holder._id").is(holderId)
                .and("resource.type").is(resourceTypeName)
                .and("resource._id").is(resourceId);

        return template.exists(query(criteria), collectionName);
    }

    @Override
    public Flux<Permission> findPermissionsByHolder(Holder holder) {
        MongoHolderType holderType = MongoHolderTypeSerializer.serialize(holder.getType());
        String holderId = holder.getId().getValue();

        Criteria criteria = where("holder.type").is(holderType)
                .and("holder._id").is(holderId);

        return template.find(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Flux<Permission> findPermissionsByHolderAndResourceType(Holder holder, ResourceType resourceType) {
        MongoHolderType holderType = MongoHolderTypeSerializer.serialize(holder.getType());
        String holderId = holder.getId().getValue();
        String resourceTypeName = resourceType.getName();

        Criteria criteria = where("holder.type").is(holderType)
                .and("holder._id").is(holderId)
                .and("resource.type").is(resourceTypeName);

        return template.find(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Flux<Permission> findPermissionsByHolderAndResourceTypeAndAction(
            Holder holder,
            ResourceType resourceType,
            Action action
    ) {
        MongoHolderType holderType = MongoHolderTypeSerializer.serialize(holder.getType());
        String holderId = holder.getId().getValue();
        String resourceTypeName = resourceType.getName();
        String actionName = action.getName();

        Criteria criteria = where("holder.type").is(holderType)
                .and("holder._id").is(holderId)
                .and("resource.type").is(resourceTypeName)
                .and("action").is(actionName);

        return template.find(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Flux<Permission> findPermissionsByHolderAndResource(Holder holder, Resource resource) {
        MongoHolderType holderType = MongoHolderTypeSerializer.serialize(holder.getType());
        String holderId = holder.getId().getValue();
        String resourceTypeName = resource.getType().getName();
        String resourceId = resource.getId()
                .map(ResourceId::getValue)
                .orElse(null);

        Criteria criteria = where("holder.type").is(holderType)
                .and("holder._id").is(holderId)
                .and("resource.type").is(resourceTypeName)
                .and("resource._id").is(resourceId);

        return template.find(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Flux<Permission> findPermissionsByResourceTypeAndAction(ResourceType resourceType, Action action) {
        String resourceTypeName = resourceType.getName();
        String actionName = action.getName();

        Criteria criteria = where("resource.type").is(resourceTypeName)
                .and("action").is(actionName);

        return template.find(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Flux<Permission> removeByHolder(Holder holder) {
        MongoHolderType holderType = MongoHolderTypeSerializer.serialize(holder.getType());
        String holderId = holder.getId().getValue();

        Criteria criteria = where("holder.type").is(holderType)
                .and("holder._id").is(holderId);

        return template.findAllAndRemove(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Flux<Permission> removeByResource(Resource resource) {
        String resourceTypeName = resource.getType().getName();
        String resourceId = resource.getId()
                .map(ResourceId::getValue)
                .orElse(null);

        Criteria criteria = where("resource.type").is(resourceTypeName)
                .and("resource._id").is(resourceId);

        return template.findAllAndRemove(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Flux<Permission> removeByHolderAndResource(Holder holder, Resource resource) {
        MongoHolderType holderType = MongoHolderTypeSerializer.serialize(holder.getType());
        String holderId = holder.getId().getValue();
        String resourceTypeName = resource.getType().getName();
        String resourceId = resource.getId()
                .map(ResourceId::getValue)
                .orElse(null);

        Criteria criteria = where("holder.type").is(holderType)
                .and("holder._id").is(holderId)
                .and("resource.type").is(resourceTypeName)
                .and("resource._id").is(resourceId);

        return template.findAllAndRemove(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Mono<Permission> removeByPermission(Permission permission) {
        String actionName = permission.getAction().getName();
        MongoHolderType holderType = MongoHolderTypeSerializer.serialize(permission.getHolder().getType());
        String holderId = permission.getHolder().getId().getValue();
        String resourceTypeName = permission.getResource()
                .getType()
                .getName();
        String resourceId = permission.getResource()
                .getId()
                .map(ResourceId::getValue)
                .orElse(null);

        Criteria criteria = where("action").is(actionName)
                .and("holder.type").is(holderType)
                .and("holder._id").is(holderId)
                .and("resource.type").is(resourceTypeName)
                .and("resource._id").is(resourceId);

        return template.findAndRemove(query(criteria), MongoPermission.class, collectionName)
                .map(MongoPermissionSerializer::deserialize);
    }

    @Override
    public Flux<Permission> removePermissions(Permission... permissions) {
        return Flux.fromIterable(Set.of(permissions))
                .flatMap(this::removeByPermission);
    }

    private Flux<Permission> bulkInsert(Collection<Permission> permissions) {
        List<MongoPermission> mongoPermissions = permissions.stream()
                .map(p -> MongoPermissionSerializer.serialize(PermissionId.create(), p, Instant.now()))
                .toList();

        return template.bulkOps(UNORDERED, MongoPermission.class, collectionName)
                .insert(mongoPermissions)
                .execute()
                .onErrorResume(
                        DuplicateKeyException.class, e -> {
                            log.warn("Tried to insert duplicate permissions. Ignoring.");

                            if (e.getCause() instanceof MongoBulkWriteException bulkWriteException) {
                                return Mono.just(bulkWriteException.getWriteResult());
                            }

                            return Mono.error(e);
                        }
                )
                .map(result -> result.getInserts()
                        .stream()
                        .map(i -> i.getId().asString().getValue())
                        .collect(Collectors.toSet()))
                .filter(ids -> !ids.isEmpty())
                .flatMapMany(ids -> template.find(query(where("_id").in(ids)), MongoPermission.class, collectionName))
                .map(MongoPermissionSerializer::deserialize);
    }

    private Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        IndexDefinition permissionsIndex = new CompoundIndexDefinition(new Document()
                .append("action", 1)
                .append("holder.type", 1)
                .append("holder._id", 1)
                .append("resource.type", 1)
                .append("resource._id", 1))
                .unique();

        IndexDefinition holderIndex = new CompoundIndexDefinition(new Document()
                .append("holder.type", 1)
                .append("holder._id", 1));

        IndexDefinition resourceIndex = new CompoundIndexDefinition(new Document()
                .append("resource.type", 1)
                .append("resource._id", 1));

        IndexDefinition actionIndex = new Index().on("action", ASC);

        return Mono.zip(
                indexOps.createIndex(permissionsIndex),
                indexOps.createIndex(holderIndex),
                indexOps.createIndex(resourceIndex),
                indexOps.createIndex(actionIndex)
        ).then();
    }

}
