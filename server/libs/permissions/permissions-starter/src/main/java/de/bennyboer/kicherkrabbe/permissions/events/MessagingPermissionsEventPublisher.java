package de.bennyboer.kicherkrabbe.permissions.events;

import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxEntry;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import de.bennyboer.kicherkrabbe.messaging.target.MessageTarget;
import de.bennyboer.kicherkrabbe.permissions.Permission;
import de.bennyboer.kicherkrabbe.permissions.ResourceType;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionEvent;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionEventType;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class MessagingPermissionsEventPublisher implements PermissionsEventPublisher {

    private final MessagingOutbox outbox;

    private final Clock clock;

    @Override
    public Mono<Void> publish(PermissionEvent event) {
        Map<ResourceType, PermissionEvent> eventsByResourceType = splitEventByResourceType(event);
        List<MessagingOutboxEntry> entries = eventsByResourceType.entrySet()
                .stream()
                .map(entry -> toEntry(entry.getKey(), entry.getValue()))
                .toList();

        return outbox.insert(entries);
    }

    private Map<ResourceType, PermissionEvent> splitEventByResourceType(PermissionEvent event) {
        return event.getPermissions()
                .stream()
                .collect(Collectors.groupingBy(
                        e -> e.getResource().getType(),
                        Collectors.collectingAndThen(Collectors.toSet(), permissions -> {
                            if (event.getType() == PermissionEventType.ADDED) {
                                return PermissionEvent.added(permissions);
                            }

                            return PermissionEvent.removed(permissions);
                        })
                ));
    }

    private MessagingOutboxEntry toEntry(ResourceType resourceType, PermissionEvent event) {
        String normalizedResourceType = resourceType
                .getName()
                .toLowerCase(Locale.ROOT);
        String exchangeName = normalizedResourceType + ".permissions";
        var exchange = ExchangeTarget.of(exchangeName);
        var target = MessageTarget.exchange(exchange);

        String eventName = event.getType()
                .name()
                .toLowerCase(Locale.ROOT);
        var routingKey = RoutingKey.ofParts("events", eventName);

        Map<String, Object> payload = Map.of(
                "type", event.getType().name(),
                "permissions", event.getPermissions()
                        .stream()
                        .map(this::serializePermission)
                        .toList()
        );

        return MessagingOutboxEntry.create(
                target,
                routingKey,
                payload,
                clock
        );
    }

    private Map<String, Object> serializePermission(Permission permission) {
        Map<String, String> holder = Map.of(
                "type", permission.getHolder().getType().name(),
                "id", permission.getHolder().getId().getValue()
        );

        Map<String, String> resource = new HashMap<>(Map.of(
                "type", permission.getResource().getType().getName()
        ));
        permission.getResource()
                .getId()
                .ifPresent(id -> resource.put("id", id.getValue()));

        return Map.of(
                "holder", holder,
                "action", permission.getAction().getName(),
                "resource", resource
        );
    }

}
