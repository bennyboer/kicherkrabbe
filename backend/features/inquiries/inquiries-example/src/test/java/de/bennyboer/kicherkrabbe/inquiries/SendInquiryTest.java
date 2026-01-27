package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.inquiries.api.SenderDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.requests.SendInquiryRequest;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

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

        // and: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: sending inquiries is enabled
        enableSendingInquiries();

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
    void shouldRefuseToSendInquiryWhenThePermissionIsMissing() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe+test@example.com";
        request.sender.phone = "+49 1234 5678 9999";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: sending inquiries is enabled
        enableSendingInquiries();

        // when: sending the inquiry; then: the inquiry sending is refused
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
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

        // and: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: sending inquiries is enabled
        enableSendingInquiries();

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
        )).matches(e -> e.getCause() instanceof TooManyRequestsException);
    }

    @Test
    void shouldRefuseToSendInquiryThatExceedsTheMaximumSenderNameLength() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe with a really long name that exceeds the maximum length of 200 characters. "
                + "Here are some fill words that make it longer. Here are some fill words that make it longer. Here "
                + "are some fill words that make it longer.";
        request.sender.mail = "john.doe+test@example.com";
        request.sender.phone = "+49 1234 5678 9999";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";

        // and: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: sending inquiries is enabled
        enableSendingInquiries();

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
        request.sender.name = "John Doe with a really long name that exceeds the maximum length of 200 characters. "
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

        // and: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: sending inquiries is enabled
        enableSendingInquiries();

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
        request.sender.mail = "john.doeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee+test@kicherkrabbe.com";
        request.sender.phone = "+49 1234 5678 9999";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";

        // and: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: sending inquiries is enabled
        enableSendingInquiries();

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
        request.sender.mail = "john.doeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee+test@kicherkrabbe.com";
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

        // and: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: sending inquiries is enabled
        enableSendingInquiries();

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

        // and: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: sending inquiries is enabled
        enableSendingInquiries();

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
        request.sender.phone = "+49 1234 5678 9999 32894238422";
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

        // and: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: sending inquiries is enabled
        enableSendingInquiries();

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
        request.subject = "This is a subject that exceeds the maximum length of 200 characters. Here are some fill "
                + "words that make it longer and longer and longer and longer and longer and longer and longer and "
                + "longer and longer and longer and longer and longer and longer and longer!";
        request.message = "This is a test inquiry!";

        // and: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: sending inquiries is enabled
        enableSendingInquiries();

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
        request.subject = "This is a subject that exceeds the maximum length of 200 characters. Here are some fill "
                + "words that make it longer and longer and longer and longer and longer and longer and longer";
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

        // and: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: sending inquiries is enabled
        enableSendingInquiries();

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

        // and: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: sending inquiries is enabled
        enableSendingInquiries();

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

        // and: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: sending inquiries is enabled
        enableSendingInquiries();

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

        // and: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: sending inquiries is disabled
        disableSendingInquiries();

        // when: sending the inquiry; then: the inquiry sending is refused
        assertThatThrownBy(() -> sendInquiry(
                request.requestId,
                request.sender,
                request.subject,
                request.message,
                Agent.anonymous()
        )).matches(e -> e.getCause() instanceof InquiriesDisabledException);


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
    void shouldRefuseToSendInquiryWhenSendingMoreThanNInquiriesWithTheSameEMailWithinACertainTimeFrame() {
        // given: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: there is a maximum of 2 inquiries per e-mail address within 24 hours
        setMaximumInquiriesPerEmailPerTimeFrame(2, Duration.ofHours(24));

        // and: the other rate limits are effectively disabled by setting their maximum to 999
        setMaximumInquiriesPerTimeFrame(999, Duration.ofHours(24));
        setMaximumInquiriesPerIPAddressPerTimeFrame(999, Duration.ofHours(24));

        // and: sending inquiries is enabled
        enableSendingInquiries();

        // and: we are at a fixed point in time
        setTime(Instant.parse("2024-03-12T12:30:00Z"));

        // and: a inquiry is sent
        var sender = new SenderDTO();
        sender.name = "John Doe";
        sender.mail = "john.doe+test@example.de";
        sender.phone = "+49 1234 5678 9999";
        sendInquiry(
                "REQUEST_ID_1",
                sender,
                "Test inquiry 1",
                "This is a test inquiry!",
                Agent.anonymous()
        );

        // and: another inquiry is sent 5 minutes later
        setTime(Instant.parse("2024-03-12T12:35:00Z"));
        sendInquiry(
                "REQUEST_ID_2",
                sender,
                "Test inquiry 2",
                "This is a test inquiry!",
                Agent.anonymous()
        );

        // when: another inquiry is sent 5 minutes later; then: the inquiry sending is refused
        setTime(Instant.parse("2024-03-12T12:40:00Z"));
        assertThatThrownBy(() -> sendInquiry(
                "REQUEST_ID_3",
                sender,
                "Test inquiry 3",
                "This is a test inquiry!",
                Agent.anonymous()
        )).matches(e -> e.getCause() instanceof TooManyRequestsException);

        // when: another inquiry is sent with another e-mail address; then: the inquiry sending is accepted
        var otherSender = new SenderDTO();
        otherSender.name = "Jane Doe";
        otherSender.mail = "jane.doe+test@example.de";
        otherSender.phone = "+49 1234 5678 9999";
        assertThatNoException().isThrownBy(() -> sendInquiry(
                "REQUEST_ID_4",
                otherSender,
                "Test inquiry 4",
                "This is a test inquiry!",
                Agent.anonymous()
        ));

        // when: another inquiry is sent 23 hours later; then: the inquiry sending is denied as well
        setTime(Instant.parse("2024-03-13T11:30:00Z"));
        assertThatThrownBy(() -> sendInquiry(
                "REQUEST_ID_5",
                sender,
                "Test inquiry 5",
                "This is a test inquiry!",
                Agent.anonymous()
        )).matches(e -> e.getCause() instanceof TooManyRequestsException);

        // when: another inquiry is sent 24 hours later; then: the inquiry sending is accepted
        setTime(Instant.parse("2024-03-13T12:30:00Z"));
        assertThatNoException().isThrownBy(() -> sendInquiry(
                "REQUEST_ID_6",
                sender,
                "Test inquiry 6",
                "This is a test inquiry!",
                Agent.anonymous()
        ));
    }

    @Test
    void shouldRefuseToSendInquiryWhenSendingMoreThanNInquiriesWithinACertainTimeFrameFromTheSameIPAddress() {
        // given: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: there is a maximum of 2 inquiries per IP address within 24 hours
        setMaximumInquiriesPerIPAddressPerTimeFrame(2, Duration.ofHours(24));

        // and: the other rate limits are effectively disabled by setting their maximum to 999
        setMaximumInquiriesPerEmailPerTimeFrame(999, Duration.ofHours(24));
        setMaximumInquiriesPerTimeFrame(999, Duration.ofHours(24));

        // and: sending inquiries is enabled
        enableSendingInquiries();

        // and: we are at a fixed point in time
        setTime(Instant.parse("2024-03-12T12:30:00Z"));

        // and: a inquiry is sent
        var sender = new SenderDTO();
        sender.name = "John Doe";
        sender.mail = "john.doe+test@example.de";
        sender.phone = "+49 1234 5678 9999";
        sendInquiry(
                "REQUEST_ID_1",
                sender,
                "Test inquiry 1",
                "This is a test inquiry!",
                Agent.anonymous(),
                "159.185.44.88"
        );

        // and: another inquiry is sent 5 minutes later
        setTime(Instant.parse("2024-03-12T12:35:00Z"));
        sendInquiry(
                "REQUEST_ID_2",
                sender,
                "Test inquiry 2",
                "This is a test inquiry!",
                Agent.anonymous(),
                "159.185.44.88"
        );

        // when: another inquiry is sent 5 minutes later; then: the inquiry sending is refused
        setTime(Instant.parse("2024-03-12T12:40:00Z"));
        assertThatThrownBy(() -> sendInquiry(
                "REQUEST_ID_3",
                sender,
                "Test inquiry 3",
                "This is a test inquiry!",
                Agent.anonymous(),
                "159.185.44.88"
        )).matches(e -> e.getCause() instanceof TooManyRequestsException);

        // when: another inquiry is sent with another IP address; then: the inquiry sending is accepted
        assertThatNoException().isThrownBy(() -> sendInquiry(
                "REQUEST_ID_4",
                sender,
                "Test inquiry 4",
                "This is a test inquiry!",
                Agent.anonymous(),
                "98.249.119.114"
        ));

        // when: another inquiry is sent 23 hours later; then: the inquiry sending is denied as well
        setTime(Instant.parse("2024-03-13T11:30:00Z"));
        assertThatThrownBy(() -> sendInquiry(
                "REQUEST_ID_5",
                sender,
                "Test inquiry 5",
                "This is a test inquiry!",
                Agent.anonymous(),
                "159.185.44.88"
        )).matches(e -> e.getCause() instanceof TooManyRequestsException);

        // when: another inquiry is sent 24 hours later; then: the inquiry sending is accepted
        setTime(Instant.parse("2024-03-13T12:30:00Z"));
        assertThatNoException().isThrownBy(() -> sendInquiry(
                "REQUEST_ID_6",
                sender,
                "Test inquiry 6",
                "This is a test inquiry!",
                Agent.anonymous(),
                "159.185.44.88"
        ));
    }

    @Test
    void shouldRefuseToSendInquiryWhenSendingMoreThanNInquiriesWithinACertainTimeFrame() {
        // given: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: the currently logged in user is able to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: there is a maximum of 5 inquiries within 24 hours
        setMaximumInquiriesPerTimeFrame(5, Duration.ofHours(24));

        // and: the other rate limits are effectively disabled by setting their maximum to 999
        setMaximumInquiriesPerEmailPerTimeFrame(999, Duration.ofHours(24));
        setMaximumInquiriesPerIPAddressPerTimeFrame(999, Duration.ofHours(24));

        // and: sending inquiries is enabled
        enableSendingInquiries();

        // and: we are at a fixed point in time
        setTime(Instant.parse("2024-03-12T12:30:00Z"));

        // and: a inquiry is sent
        var sender = new SenderDTO();
        sender.name = "John Doe";
        sender.mail = "john.doe+test@example.de";
        sender.phone = "+49 1234 5678 9999";
        sendInquiry(
                "REQUEST_ID_1",
                sender,
                "Test inquiry 1",
                "This is a test inquiry!",
                Agent.anonymous()
        );

        // and: another inquiry is sent 5 minutes later
        setTime(Instant.parse("2024-03-12T12:35:00Z"));
        sendInquiry(
                "REQUEST_ID_2",
                sender,
                "Test inquiry 2",
                "This is a test inquiry!",
                Agent.anonymous()
        );

        // when: another inquiry is sent 5 minutes later
        setTime(Instant.parse("2024-03-12T12:40:00Z"));
        sendInquiry(
                "REQUEST_ID_3",
                sender,
                "Test inquiry 3",
                "This is a test inquiry!",
                Agent.anonymous()
        );

        // and: another inquiry is sent 5 minutes later
        setTime(Instant.parse("2024-03-12T12:45:00Z"));
        sendInquiry(
                "REQUEST_ID_4",
                sender,
                "Test inquiry 4",
                "This is a test inquiry!",
                Agent.anonymous()
        );

        // and: another inquiry is sent 5 minutes later
        setTime(Instant.parse("2024-03-12T12:50:00Z"));
        sendInquiry(
                "REQUEST_ID_5",
                sender,
                "Test inquiry 5",
                "This is a test inquiry!",
                Agent.anonymous()
        );

        // when: another inquiry is sent 5 minutes later; then: the inquiry sending is refused
        setTime(Instant.parse("2024-03-12T12:55:00Z"));
        assertThatThrownBy(() -> sendInquiry(
                "REQUEST_ID_6",
                sender,
                "Test inquiry 6",
                "This is a test inquiry!",
                Agent.anonymous()
        )).matches(e -> e.getCause() instanceof TooManyRequestsException);

        // when: another inquiry is sent 23 hours later; then: the inquiry sending is denied as well
        setTime(Instant.parse("2024-03-13T11:30:00Z"));
        assertThatThrownBy(() -> sendInquiry(
                "REQUEST_ID_7",
                sender,
                "Test inquiry 7",
                "This is a test inquiry!",
                Agent.anonymous()
        )).matches(e -> e.getCause() instanceof TooManyRequestsException);

        // when: another inquiry is sent 24 hours later; then: the inquiry sending is accepted
        setTime(Instant.parse("2024-03-13T12:30:00Z"));
        assertThatNoException().isThrownBy(() -> sendInquiry(
                "REQUEST_ID_8",
                sender,
                "Test inquiry 8",
                "This is a test inquiry!",
                Agent.anonymous()
        ));
    }

}

