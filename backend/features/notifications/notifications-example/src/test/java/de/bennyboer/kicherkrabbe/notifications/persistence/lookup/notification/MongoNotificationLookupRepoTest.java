package de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification;

import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.mongo.MongoLookupNotification;
import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.mongo.MongoNotificationLookupRepo;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoNotificationLookupRepoTest extends NotificationLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoNotificationLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected NotificationLookupRepo createRepo() {
        return new MongoNotificationLookupRepo("notifications_lookup", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupNotification.class)
                .inCollection("notifications_lookup")
                .all()
                .block();
    }

}
