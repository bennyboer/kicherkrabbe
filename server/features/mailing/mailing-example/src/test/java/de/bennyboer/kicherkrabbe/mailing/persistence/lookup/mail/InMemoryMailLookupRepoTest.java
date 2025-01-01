package de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail;

import de.bennyboer.kicherkrabbe.mailing.persistence.lookup.mail.inmemory.InMemoryMailLookupRepo;

public class InMemoryMailLookupRepoTest extends MailLookupRepoTest {

    @Override
    protected MailLookupRepo createRepo() {
        return new InMemoryMailLookupRepo();
    }

}
