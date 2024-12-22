package de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.inquiries.InquiryId;
import de.bennyboer.kicherkrabbe.inquiries.RequestId;
import de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.InquiryLookupRepo;
import de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.LookupInquiry;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoInquiryLookupRepo
        extends MongoEventSourcingReadModelRepo<InquiryId, LookupInquiry, MongoLookupInquiry>
        implements InquiryLookupRepo {

    public MongoInquiryLookupRepo(ReactiveMongoTemplate template) {
        this("inquiries_lookup", template);
    }

    public MongoInquiryLookupRepo(String collectionName, ReactiveMongoTemplate template) {
        super(collectionName, template, new MongoLookupInquirySerializer());
    }

    @Override
    public Mono<LookupInquiry> find(InquiryId id) {
        Criteria criteria = where("_id").is(id.getValue());
        Query query = query(criteria);

        return template.findOne(query, MongoLookupInquiry.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Mono<LookupInquiry> findByRequestId(RequestId requestId) {
        Criteria criteria = where("requestId").is(requestId.getValue());
        Query query = query(criteria);

        return template.findOne(query, MongoLookupInquiry.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        Index requestIdIndex = new Index().on("requestId", ASC);

        return indexOps.ensureIndex(requestIdIndex)
                .then();
    }

    @Override
    protected String stringifyId(InquiryId inquiryId) {
        return inquiryId.getValue();
    }

}
