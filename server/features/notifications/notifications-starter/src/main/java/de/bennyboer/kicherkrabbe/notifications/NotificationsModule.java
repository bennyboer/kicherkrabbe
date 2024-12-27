package de.bennyboer.kicherkrabbe.notifications;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.notifications.api.requests.ActivateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.notifications.api.requests.DeactivateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.notifications.api.requests.UpdateSystemChannelRequest;
import de.bennyboer.kicherkrabbe.notifications.api.responses.*;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class NotificationsModule {

    public Mono<QueryNotificationsResponse> getNotifications(
            DateRangeFilter dateRangeFilter,
            long skip,
            long limit,
            Agent agent
    ) {
        return Mono.empty(); // TODO
    }

    public Mono<QuerySettingsResponse> getSettings(Agent agent) {
        return Mono.empty(); // TODO
    }

    public Mono<EnableSystemNotificationsResponse> enableSystemNotifications(long version, Agent agent) {
        return Mono.empty(); // TODO
    }

    public Mono<DisableSystemNotificationsResponse> disableSystemNotifications(long version, Agent agent) {
        return Mono.empty(); // TODO
    }

    public Mono<UpdateSystemChannelResponse> updateSystemChannel(UpdateSystemChannelRequest request, Agent agent) {
        return Mono.empty(); // TODO
    }

    public Mono<ActivateSystemChannelResponse> activateSystemChannel(
            ActivateSystemChannelRequest request,
            Agent agent
    ) {
        return Mono.empty(); // TODO
    }

    public Mono<DeactivateSystemChannelResponse> deactivateSystemChannel(
            DeactivateSystemChannelRequest request,
            Agent agent
    ) {
        return Mono.empty(); // TODO
    }

}
