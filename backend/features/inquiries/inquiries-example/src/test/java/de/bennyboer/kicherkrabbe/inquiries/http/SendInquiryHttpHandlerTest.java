package de.bennyboer.kicherkrabbe.inquiries.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.inquiries.InquiriesDisabledException;
import de.bennyboer.kicherkrabbe.inquiries.TooManyRequestsException;
import de.bennyboer.kicherkrabbe.inquiries.api.SenderDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.requests.SendInquiryRequest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class SendInquiryHttpHandlerTest extends HttpHandlerTest {

    @Test
    void shouldSuccessfullySendInquiry() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe+test@example.com";
        request.sender.phone = "+49 1234 5678 9999";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";

        // and: the module is configured to return a successful response
        when(module.sendInquiry(
                eq(request.requestId),
                eq(request.sender),
                eq(request.subject),
                eq(request.message),
                eq(Agent.anonymous()),
                any()
        )).thenReturn(Mono.empty());

        // when: posting the request
        var exchange = client.post()
                .uri("/api/inquiries/send")
                .bodyValue(request)
                .exchange();

        // then: the response is successful
        exchange.expectStatus().isOk();

        // and: the response does not contain a body
        exchange.expectBody().isEmpty();
    }

    @Test
    void shouldRespondWithBadRequestOnInvalidRequest() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";

        // and: the module is configured to return an illegal argument exception
        when(module.sendInquiry(
                eq(request.requestId),
                eq(request.sender),
                eq(request.subject),
                eq(request.message),
                eq(Agent.anonymous()),
                any()
        )).thenReturn(Mono.error(new IllegalArgumentException("Invalid request")));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/inquiries/send")
                .bodyValue(request)
                .exchange();

        // then: the response is bad request
        exchange.expectStatus().isBadRequest();
    }

    @Test
    void shouldRespondWithForbiddenIfInquiriesAreDisabled() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe+test@example.com";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";

        // and: the module is configured to return a forbidden response
        when(module.sendInquiry(
                eq(request.requestId),
                eq(request.sender),
                eq(request.subject),
                eq(request.message),
                eq(Agent.anonymous()),
                any()
        )).thenReturn(Mono.error(new InquiriesDisabledException()));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/inquiries/send")
                .bodyValue(request)
                .exchange();

        // then: the response is forbidden
        exchange.expectStatus().isForbidden();
    }

    @Test
    void shouldRespondWithTooManyRequestsIfRateLimitExceeded() {
        // given: a request to send an inquiry
        var request = new SendInquiryRequest();
        request.requestId = "REQUEST_ID";
        request.sender = new SenderDTO();
        request.sender.name = "John Doe";
        request.sender.mail = "john.doe+test@example.com";
        request.subject = "Test inquiry";
        request.message = "This is a test inquiry!";

        // and: the module is configured to return a too many requests response
        when(module.sendInquiry(
                eq(request.requestId),
                eq(request.sender),
                eq(request.subject),
                eq(request.message),
                eq(Agent.anonymous()),
                any()
        )).thenReturn(Mono.error(new TooManyRequestsException()));

        // when: posting the request
        var exchange = client.post()
                .uri("/api/inquiries/send")
                .bodyValue(request)
                .exchange();

        // then: the response is too many requests
        exchange.expectStatus().isEqualTo(429);
    }

}
