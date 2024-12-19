package de.bennyboer.kicherkrabbe.inquiries.persistence.lookup;

import de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.inmemory.InMemoryInquiryLookupRepo;

public class InMemoryInquiryLookupRepoTest extends InquiryLookupRepoTest {

    @Override
    protected InquiryLookupRepo createRepo() {
        return new InMemoryInquiryLookupRepo();
    }

}
