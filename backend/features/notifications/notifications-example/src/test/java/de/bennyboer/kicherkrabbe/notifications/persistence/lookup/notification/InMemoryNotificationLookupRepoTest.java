package de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification;

import de.bennyboer.kicherkrabbe.notifications.persistence.lookup.notification.inmemory.InMemoryNotificationLookupRepo;

public class InMemoryNotificationLookupRepoTest extends NotificationLookupRepoTest {

    @Override
    protected NotificationLookupRepo createRepo() {
        return new InMemoryNotificationLookupRepo();
    }

}
