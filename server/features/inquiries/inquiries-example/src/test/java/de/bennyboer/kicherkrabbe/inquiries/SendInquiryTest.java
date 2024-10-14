package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.inquiries.api.SenderDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.requests.SendInquiryRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SendInquiryTest extends InquiriesModuleTest {

    @Test
    void shouldSendInquiry() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe+test@example.com";
        request.sender.phone = "+49 1234 5678 9999";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";

        // when: sending the inquiry
        sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        );

        // then: the inquiry has been sent
        var inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNotNull();
        assertThat(inquiry.sender.name).isEqualTo(request.sender.name);
        assertThat(inquiry.sender.mail).isEqualTo(request.sender.mail);
        assertThat(inquiry.sender.phone).isEqualTo(request.sender.phone);
        assertThat(inquiry.subject).isEqualTo(request.subject);
        assertThat(inquiry.message).isEqualTo(request.message);
        assertThat(inquiry.sentAt).isNotNull();
    }

    @Test
    void shouldRefuseToSendInquiryWhenRequestIdHasAlreadyBeenSeen() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe+test@example.com";
        request.sender.phone = "+49 1234 5678 9999";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";

        // and: the inquiry has already been sent
        sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        );

        // when: sending the inquiry again; then: the inquiry sending is refused
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(TooManyRequestsException.class);
    }

    // TODO test validation

    // TODO test that we wont accept sending an inquiry if sending an inquiry is currently disabled

    // TODO test that we wont accept n inquiries from the same e-mail address within a certain time frame

    // TODO test that we wont accept n inquiries from the same IP address within a certain time frame

    // TODO test that we wont acdept n inquiries in general within a certain time frame

}
