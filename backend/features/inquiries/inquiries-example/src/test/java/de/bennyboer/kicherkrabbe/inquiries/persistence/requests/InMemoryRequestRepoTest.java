package de.bennyboer.kicherkrabbe.inquiries.persistence.requests;

import de.bennyboer.kicherkrabbe.inquiries.persistence.requests.inmemory.InMemoryRequestRepo;

public class InMemoryRequestRepoTest extends RequestRepoTest {

    @Override
    protected RequestRepo createRepo() {
        return new InMemoryRequestRepo();
    }

}
