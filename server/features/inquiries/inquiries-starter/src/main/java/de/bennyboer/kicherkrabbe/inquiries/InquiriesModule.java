package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.inquiries.api.InquiryDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.SenderDTO;
import jakarta.annotation.Nullable;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class InquiriesModule {

    public Mono<Void> sendInquiry(
            String requestId,
            SenderDTO sender,
            String subject,
            String message,
            Agent agent,
            @Nullable String ipAddress
    ) {
        // TODO Validate input (sanitize message, subject, sender name, sender mail, sender phone)
        // TODO Check if inputs are in required format (mail)
        // TODO Check if inputs are within limits (message, subject, name, mail, phone length)
        // TODO Check if inputs are not empty or null (except for sender phone)
        // TODO Check if we haven't seen requestId before, otherwise throw TooManyRequestsException
        // TODO Check if we are not disabled, otherwise throw InquiriesDisabledException
        // TODO Check if we are not rate limited (see inquiries settings), otherwise throw TooManyRequestsException

        return Mono.empty(); // TODO
    }

    public Mono<InquiryDTO> getInquiryByRequestId(String requestId, Agent agent) {
        // TODO Check whether the caller has the permission to see the inquiry (no one really has, but we should
        //  check anyway)
        return Mono.empty(); // TODO
    }

    public Mono<Void> setSendingInquiriesEnabled(boolean enabled, Agent agent) {
        // TODO Check whether caller has permission
        return Mono.empty(); // TODO
    }

    public Mono<Void> setMaximumInquiriesPerEmailPerTimeFrame(int count, Duration duration, Agent agent) {
        // TODO Check whether caller has permission
        return Mono.empty(); // TODO
    }

    public Mono<Void> setMaximumInquiriesPerIPAddressPerTimeFrame(int count, Duration duration, Agent agent) {
        // TODO Check whether caller has permission
        return Mono.empty(); // TODO
    }

    public Mono<Void> setMaximumInquiriesPerTimeFrame(int count, Duration duration, Agent agent) {
        // TODO Check whether caller has permission
        return Mono.empty(); // TODO
    }
    
}