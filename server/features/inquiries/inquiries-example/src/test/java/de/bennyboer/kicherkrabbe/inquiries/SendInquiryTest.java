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

    @Test
    void shouldRefuseToSendInquiryThatExceedsTheMaximumSenderNameLength() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe with a really long name that exceeds the maximum length of 128 characters. "
                + "Here are some fill words that make it longer.";
        request.sender.mail = "john.doe+test@example.com";
        request.sender.phone = "+49 1234 5678 9999";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";

        // when: sending the inquiry; then: the inquiry sending is refused
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        var inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: shorting the sender name and sending the inquiry again
        request.sender.name = "John Doe with a really long name that exceeds the maximum length of 128 characters. "
                + "Here are some fill words that make it long.";
        sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        );

        // then: the inquiry has been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNotNull();
    }

    @Test
    void shouldRefuseToSendInquiryWithAnInvalidSenderName() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John <script>alert('XSS');</script> Doe";
        request.sender.mail = "john.doe+test@example.com";
        request.sender.phone = "+49 1234 5678 9999";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";

        // when: sending the inquiry; then: the inquiry sending is refused
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        var inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using null as name and sending the inquiry again
        request.sender.name = null;
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using an empty string as name and sending the inquiry again
        request.sender.name = "";
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();
    }

    @Test
    void shouldRefuseToSendInquiryThatExceedsTheMaximumSenderMailLength() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john"
                +
                ".doeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee+test@example.com";
        request.sender.phone = "+49 1234 5678 9999";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";

        // when: sending the inquiry; then: the inquiry sending is refused
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        var inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: shorting the sender mail and sending the inquiry again
        request.sender.mail = "john"
                +
                ".doeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee+test@example.com";
        sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        );

        // then: the inquiry has been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNotNull();
    }

    @Test
    void shouldRefuseToSendInquiryWithAnInvalidSenderEMail() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john";
        request.sender.phone = "+49 1234 5678 9999";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";

        // when: sending the inquiry; then: the inquiry sending is refused
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        var inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using another invalid e-mail address and sending the inquiry again
        request.sender.mail = "john.doe@example";
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using another invalid e-mail address and sending the inquiry again
        request.sender.mail = "@example.de";
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using another invalid e-mail address and sending the inquiry again
        request.sender.mail = "john.doe@";
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using another invalid e-mail address and sending the inquiry again
        request.sender.mail = "john.doe@example.";
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());

        // when: using another invalid e-mail address containing a no-break space and sending the inquiry again
        request.sender.mail = "john.doe @example.de";
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using null as e-mail address and sending the inquiry again
        request.sender.mail = null;
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using an empty string as e-mail address and sending the inquiry again
        request.sender.mail = "";
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using another invalid e-mail address and sending the inquiry again
        request.sender.mail = "<script>alert('XSS');</script>";
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();
    }

    @Test
    void shouldRefuseToSendInquiryThatExceedsTheMaximumSenderPhoneLength() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe+test@example.com";
        request.sender.phone = "+49 1234 5678 9999 32894238422833";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";

        // when: sending the inquiry; then: the inquiry sending is refused
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        var inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: shorting the sender phone and sending the inquiry again
        request.sender.phone = "+49 1234 5678 9999 3289423842283";
        sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        );

        // then: the inquiry has been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNotNull();
    }

    @Test
    void shouldRefuseToSendInquiryWithAnInvalidSenderPhone() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe+test@example.de";
        request.sender.phone = "My phone number";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";

        // when: sending the inquiry; then: the inquiry sending is refused
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        var inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using another invalid phone number and sending the inquiry again
        request.sender.phone = "-38423";
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using another invalid phone number and sending the inquiry again
        request.sender.phone = "<script>alert('XSS');</script>";
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using null as phone number and sending the inquiry again
        request.sender.phone = null;
        sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        );

        // then: the inquiry has been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNotNull();
    }

    @Test
    void shouldRefuseToSendInquiryThatExceedsTheMaximumSubjectLength() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe+test@example.de";
        request.sender.phone = "+49 1234 5678 9999";
        request.subject = "This is a subject that exceeds the maximum length of 256 characters. Here are some fill "
                + "words that make it longer and longer and longer and longer and longer and longer and longer and "
                + "longer and longer and longer and longer and longer and longer and longer!";
        request.message = "This is a test inquiry!";

        // when: sending the inquiry; then: the inquiry sending is refused
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        var inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: shorting the subject and sending the inquiry again
        request.subject = "This is a subject that exceeds the maximum length of 256 characters. Here are some fill "
                + "words that make it longer and longer and longer and longer and longer and longer and longer and "
                + "longer and longer and longer and longer and longer and longer and longer";
        sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        );

        // then: the inquiry has been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNotNull();
    }

    @Test
    void shouldRefuseToSendInquiryWithAnInvalidSubject() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe+test@example.de";
        request.sender.phone = "+49 1234 5678 9999";
        request.subject = "This is a subject with an invalid character: <script>alert('XSS');</script>";
        request.message = "This is a test inquiry!";

        // when: sending the inquiry; then: the inquiry sending is refused
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        var inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using null as subject and sending the inquiry again
        request.subject = null;
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using an empty string as subject and sending the inquiry again
        request.subject = "";
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using a subject with a no-break space and sending the inquiry again
        request.subject = "This is a subject with a no-break space: ";
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();
    }

    @Test
    void shouldRefuseToSendInquiryThatExceedsTheMaximumMessageLength() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe+test@example.de";
        request.sender.phone = "+49 1234 5678 9999";
        request.subject = "Test inquiry";
        request.message = "This is a message that is way too long to fathom!!".repeat(200) + "!";

        // when: sending the inquiry; then: the inquiry sending is refused
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        var inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: shorting the message and sending the inquiry again
        request.message = "This is a message that is way too long to fathom!!".repeat(200);
        sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        );

        // then: the inquiry has been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNotNull();
    }

    @Test
    void shouldRefuseToSendInquiryWithAnInvalidMessage() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe+test@example.de";
        request.sender.phone = "+49 1234 5678 9999";
        request.subject = "Test inquiry";
        request.message = "This is a message with an invalid character: <script>alert('XSS');</script>";

        // when: sending the inquiry; then: the inquiry sending is refused
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        var inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using null as message and sending the inquiry again
        request.message = null;
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using an empty string as message and sending the inquiry again
        request.message = "";
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();

        // when: using a message with a no-break space and sending the inquiry again
        request.message = "This is a message with a no-break space: ";
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(IllegalArgumentException.class);

        // and: the inquiry has not been sent
        inquiry = getInquiryByRequestId(request.requestId, Agent.system());
        assertThat(inquiry).isNull();
    }

    @Test
    void shouldRefuseToSendInquiryWhenSendingInquiriesIsDisabled() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe+test@example.de";
        request.sender.phone = "+49 1234 5678 9999";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";

        // and: sending inquiries is disabled
        disableSendingInquiries();

        // when: sending the inquiry; then: the inquiry sending is refused
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).isInstanceOf(InquiriesDisabledException.class);

        // when: enabling sending inquiries and sending the inquiry again
        enableSendingInquiries();
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
    }

    @Test
    void shouldRefuseToSendInquiryWhenSendingMoreThanNInquiriesWithinACertainTimeFrame() {
        // given: there will be a maximum of 2 inquiries per e-mail address within 24 hours
        setMaximumInquiriesPerEmailPerTimeFrame(2, );
    }

    // TODO test that we wont accept n inquiries from the same e-mail address within a certain time frame

    // TODO test that we wont accept n inquiries from the same IP address within a certain time frame

    // TODO test that we wont acdept n inquiries in general within a certain time frame


}

