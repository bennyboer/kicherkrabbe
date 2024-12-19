package de.bennyboer.kicherkrabbe.inquiries.persistence.lookup;

import de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.mongo.MongoInquiryLookupRepo;
import de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.mongo.MongoLookupInquiry;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoInquiryLookupRepoTest extends InquiryLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoInquiryLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected InquiryLookupRepo createRepo() {
        return new MongoInquiryLookupRepo("inquiries_lookup", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupInquiry.class)
                .inCollection("inquiries_lookup")
                .all()
                .block();
    }

}
