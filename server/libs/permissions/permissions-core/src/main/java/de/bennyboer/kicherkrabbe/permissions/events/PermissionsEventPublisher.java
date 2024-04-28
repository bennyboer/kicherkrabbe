package de.bennyboer.kicherkrabbe.permissions.events;

import reactor.core.publisher.Mono;

/**
 * When permissions are managed, a lot of events happen that can be observed from the outside using
 * this event publisher. The main purpose of this event publisher is to publish events to a messaging broker.
 * But it can also be used to just log events or to do something entirely different.
 */
public interface PermissionsEventPublisher {

    Mono<Void> publish(PermissionEvent event);

}
