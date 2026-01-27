package de.bennyboer.kicherkrabbe.mailbox.persistence.lookup;

import de.bennyboer.kicherkrabbe.mailbox.persistence.lookup.inmemory.InMemoryMailLookupRepo;

public class InMemoryMailLookupRepoTest extends MailLookupRepoTest {

    @Override
    protected MailLookupRepo createRepo() {
        return new InMemoryMailLookupRepo();
    }

}
