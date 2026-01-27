package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.inquiries.api.SenderDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.requests.SendInquiryRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DeleteInquiryTest extends InquiriesModuleTest {

    @Test
    void shouldDeleteInquiry() {
        // given: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: sending inquiries is enabled
        enableSendingInquiries();

        // and: a sent inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe+test@example.com";
        request.sender.phone = "+49 1234 5678 9999";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";
        var inquiryId = sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        );

        // when: deleting the inquiry
        deleteInquiry(inquiryId, Agent.system());

        // then: the inquiry is gone
        var result = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(result).isNull();
    }

}

