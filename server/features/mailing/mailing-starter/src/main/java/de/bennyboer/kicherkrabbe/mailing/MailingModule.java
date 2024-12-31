package de.bennyboer.kicherkrabbe.mailing;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.mailing.api.requests.ClearMailgunApiTokenRequest;
import de.bennyboer.kicherkrabbe.mailing.api.requests.SendMailRequest;
import de.bennyboer.kicherkrabbe.mailing.api.requests.UpdateMailgunApiTokenRequest;
import de.bennyboer.kicherkrabbe.mailing.api.responses.ClearMailgunApiTokenResponse;
import de.bennyboer.kicherkrabbe.mailing.api.responses.QuerySettingsResponse;
import de.bennyboer.kicherkrabbe.mailing.api.responses.UpdateMailgunApiTokenResponse;
import reactor.core.publisher.Mono;

public class MailingModule {

    public Mono<Void> sendMail(SendMailRequest request, Agent system) {
        return Mono.empty(); // TODO
    }

    public Mono<QuerySettingsResponse> getSettings(Agent agent) {
        return Mono.empty(); // TODO
    }

    public Mono<UpdateMailgunApiTokenResponse> updateMailgunApiToken(
            UpdateMailgunApiTokenRequest request,
            Agent agent
    ) {
        return Mono.empty(); // TODO
    }

    public Mono<ClearMailgunApiTokenResponse> clearMailgunApiToken(
            ClearMailgunApiTokenRequest request,
            Agent agent
    ) {
        return Mono.empty(); // TODO
    }

    public Mono<Void> allowUserToReadAndManageSettings(String userId) {
        return Mono.empty(); // TODO
    }

    public Mono<Void> removePermissionsForUser(String userId) {
        return Mono.empty(); // TODO
    }

}
