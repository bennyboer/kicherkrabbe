package de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail;

import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.mongo.MongoLookupMail;
import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.mongo.MongoMailLookupRepo;
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
        return new MongoMailLookupRepo("mailing_mails_lookup", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupMail.class)
                .inCollection("mailing_mails_lookup")
                .all()
                .block();
    }

}
