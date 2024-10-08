package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.inquiries.http.api.SenderDTO;
import reactor.core.publisher.Mono;

public class InquiriesModule {

    public Mono<Void> sendInquiry(String requestId, SenderDTO sender, String subject, String message, Agent agent) {
        // TODO Validate input (sanitize message, subject, sender name, sender mail, sender phone)
        // TODO Check if inputs are in required format (mail)
        // TODO Check if inputs are within limits (message, subject, name, mail, phone length)
        // TODO Check if inputs are not empty or null (except for sender phone)
        // TODO Check if we haven't seen requestId before, otherwise throw TooManyRequestsException
        // TODO Check if we are not disabled, otherwise throw InquiriesDisabledException
        // TODO Check if we are not rate limited (see inquiries settings), otherwise throw TooManyRequestsException

        return Mono.empty(); // TODO
    }

}
