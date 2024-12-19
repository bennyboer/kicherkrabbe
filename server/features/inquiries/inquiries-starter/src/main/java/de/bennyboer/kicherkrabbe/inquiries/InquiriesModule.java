package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.inquiries.api.InquiryDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.SenderDTO;
import de.bennyboer.kicherkrabbe.inquiries.settings.Settings;
import de.bennyboer.kicherkrabbe.inquiries.settings.SettingsId;
import de.bennyboer.kicherkrabbe.inquiries.settings.SettingsService;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

@AllArgsConstructor
public class InquiriesModule {

    private static final SettingsId DEFAULT_SETTINGS = SettingsId.of("DEFAULT");

    private final InquiryService inquiryService;

    private final SettingsService settingsService;

    public Mono<Void> sendInquiry(
            String requestId,
            SenderDTO sender,
            String subject,
            String message,
            Agent agent,
            @Nullable String ipAddress
    ) {
        var internalRequestId = RequestId.of(requestId);

        var senderName = SenderName.of(sanitizeInput(sender.name));
        var mail = EMail.of(sanitizeInput(sender.mail));
        var phone = Optional.ofNullable(sender.phone)
                .map(this::sanitizeInput)
                .map(PhoneNumber::of)
                .orElse(null);

        var internalSender = Sender.of(senderName, mail, phone);
        var internalSubject = Subject.of(sanitizeInput(subject));
        var internalMessage = Message.of(sanitizeInput(message));

        var fingerprint = Fingerprint.of(ipAddress);

        // TODO Permission Check
        return assertRequestIdNotSeen(internalRequestId)
                .then(assertInquiriesEnabled())
                .then(assertNotRateLimited(mail, ipAddress))
                .then(inquiryService.send(
                        internalRequestId,
                        internalSender,
                        internalSubject,
                        internalMessage,
                        fingerprint,
                        agent
                ))
                .then();
    }

    public Mono<InquiryDTO> getInquiryByRequestId(String requestId, Agent agent) {
        // TODO Check whether the caller has the permission to see the inquiry (no one really has, but we should
        //  check anyway)
        return Mono.empty(); // TODO
    }

    public Mono<Void> setSendingInquiriesEnabled(boolean enabled, Agent agent) {
        // TODO Permission Check
        return getSettings(agent)
                .flatMap(settings -> {
                    if (enabled) {
                        return settingsService.enable(settings.getId(), settings.getVersion(), agent);
                    } else {
                        return settingsService.disable(settings.getId(), settings.getVersion(), agent);
                    }
                })
                .then();
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

    private Mono<Void> assertRequestIdNotSeen(RequestId requestId) {
        return Mono.empty(); // TODO Check if we haven't seen requestId before, otherwise throw TooManyRequestsException
    }

    private Mono<Void> assertNotRateLimited(EMail mail, @Nullable String ipAddress) {
        return Mono.empty(); // TODO Check if the mail or IP address is rate limited
    }

    private Mono<Void> assertInquiriesEnabled() {
        return getSettings(Agent.system())
                .flatMap(settings -> {
                    if (settings.isDisabled()) {
                        return Mono.error(new InquiriesDisabledException());
                    }

                    return Mono.empty();
                });
    }

    private Mono<Settings> getSettings(Agent agent) {
        return settingsService.get(DEFAULT_SETTINGS)
                .switchIfEmpty(settingsService.init(agent)
                        .flatMap(idAndVersion -> settingsService.get(idAndVersion.getId())));
    }

    private String sanitizeInput(String input) {
        notNull(input, "Input must be given");

        return Jsoup.clean(input, Safelist.basic());
    }

}
