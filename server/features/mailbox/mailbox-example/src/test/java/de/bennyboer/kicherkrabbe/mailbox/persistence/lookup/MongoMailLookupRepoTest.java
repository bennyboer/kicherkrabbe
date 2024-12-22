package de.bennyboer.kicherkrabbe.mailbox.persistence.lookup;

import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.mongo.MongoLookupMail;
import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.mongo.MongoMailLookupRepo;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoMailLookupRepoTest extends MailLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoMailLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected MailLookupRepo createRepo() {
        return new MongoMailLookupRepo("mailbox_mails_lookup", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupMail.class)
                .inCollection("mailbox_mails_lookup")
                .all()
                .block();
    }

}
