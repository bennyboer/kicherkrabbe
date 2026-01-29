package de.bennyboer.kicherkrabbe.inquiries.persistence.requests.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.inquiries.EMail;
import de.bennyboer.kicherkrabbe.inquiries.RequestId;
import de.bennyboer.kicherkrabbe.inquiries.persistence.requests.Request;
import de.bennyboer.kicherkrabbe.inquiries.persistence.requests.RequestRepo;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoRequestRepo
        extends MongoEventSourcingReadModelRepo<RequestId, Request, MongoRequest>
        implements RequestRepo {

    public MongoRequestRepo(ReactiveMongoTemplate template) {
        this("inquiries_requests", template);
    }

    public MongoRequestRepo(String collectionName, ReactiveMongoTemplate template) {
        super(collectionName, template, new MongoRequestSerializer());
    }

    @Override
    public Mono<Void> insert(Request request) {
        return update(request);
    }

    @Override
    public Mono<Request> findById(RequestId id) {
        Criteria criteria = where("_id").is(id.getValue());
        Query query = query(criteria);

        return template.findOne(query, MongoRequest.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Mono<Long> countRecentByMail(EMail mail, Instant since) {
        Criteria criteria = where("mail").is(mail.getValue())
                .and("createdAt").gte(since);
        Query query = query(criteria);

        return template.count(query, MongoRequest.class, collectionName);
    }

    @Override
    public Mono<Long> countRecentByIpAddress(String ipAddress, Instant since) {
        Criteria criteria = where("ipAddress").is(ipAddress)
                .and("createdAt").gte(since);
        Query query = query(criteria);

        return template.count(query, MongoRequest.class, collectionName);
    }

    @Override
    public Mono<Long> countRecent(Instant since) {
        Criteria criteria = where("createdAt").gte(since);
        Query query = query(criteria);

        return template.count(query, MongoRequest.class, collectionName);
    }

    @Override
    public Flux<Request> findInTimeFrame(Instant from, Instant to) {
        Criteria criteria = where("createdAt").gte(from).lt(to);
        Query query = query(criteria);

        return template.find(query, MongoRequest.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        Index mailCreatedAtIndex = new Index()
                .on("mail", ASC)
                .on("createdAt", ASC);
        Index ipAddressCreatedAtIndex = new Index()
                .on("ipAddress", ASC)
                .on("createdAt", ASC);
        Index createdAtIndex = new Index()
                .on("createdAt", ASC)
                .expire(Duration.ofDays(30));

        return indexOps.createIndex(mailCreatedAtIndex)
                .then(indexOps.createIndex(ipAddressCreatedAtIndex))
                .then(indexOps.createIndex(createdAtIndex))
                .then();
    }

    @Override
    protected boolean allowSameVersionUpdate() {
        return true;
    }

    @Override
    protected String stringifyId(RequestId requestId) {
        return requestId.getValue();
    }

}
