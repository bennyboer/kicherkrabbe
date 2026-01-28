package de.bennyboer.kicherkrabbe.permissions.events;

import de.bennyboer.kicherkrabbe.messaging.RoutingKey;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListenerFactory;
import de.bennyboer.kicherkrabbe.messaging.target.ExchangeTarget;
import de.bennyboer.kicherkrabbe.permissions.*;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class PermissionEventListenerFactory {

    private final MessageListenerFactory messageListenerFactory;

    private final JsonMapper jsonMapper;

    public Flux<PermissionEvent> listen(ResourceType resourceType, String description) {
        String normalizedResourceType = resourceType.getName().toLowerCase(Locale.ROOT);
        ExchangeTarget exchange = ExchangeTarget.of(normalizedResourceType + ".permissions");
        RoutingKey routingKey = RoutingKey.ofParts("events", "*");

        return messageListenerFactory.createTransientListener(exchange, routingKey, description)
                .flatMap(delivery -> deserializeEvent(delivery.getBody()));
    }

    private Mono<PermissionEvent> deserializeEvent(byte[] body) {
        return deserializeEventMap(body)
                .map(map -> {
                    String typeStr = map.get("type").toString();
                    PermissionEventType type = typeStr.equals("ADDED")
                            ? PermissionEventType.ADDED
                            : PermissionEventType.REMOVED;

                    List<Map<String, Object>> permissionMaps = (List<Map<String, Object>>) map.get("permissions");
                    Set<Permission> permissions = permissionMaps.stream()
                            .map(this::toPermission)
                            .collect(Collectors.toSet());

                    return type == PermissionEventType.ADDED
                            ? PermissionEvent.added(permissions)
                            : PermissionEvent.removed(permissions);
                });
    }

    private Permission toPermission(Map<String, Object> permissionMap) {
        Map<String, Object> holderMap = (Map<String, Object>) permissionMap.get("holder");
        String holderTypeStr = holderMap.get("type").toString();
        String holderIdStr = holderMap.get("id").toString();

        HolderType holderType = holderTypeStr.equals("USER")
                ? HolderType.USER
                : HolderType.GROUP;
        HolderId holderId = HolderId.of(holderIdStr);
        Holder holder = holderType == HolderType.USER
                ? Holder.user(holderId)
                : Holder.group(holderId);

        String actionStr = permissionMap.get("action").toString();
        Action action = Action.of(actionStr);

        Map<String, Object> resourceMap = (Map<String, Object>) permissionMap.get("resource");
        String resourceTypeStr = resourceMap.get("type").toString();
        ResourceType resourceType = ResourceType.of(resourceTypeStr);

        Resource resource = resourceMap.containsKey("id")
                ? Resource.of(resourceType, ResourceId.of(resourceMap.get("id").toString()))
                : Resource.ofType(resourceType);

        return Permission.builder()
                .holder(holder)
                .isAllowedTo(action)
                .on(resource);
    }

    private Mono<Map<String, Object>> deserializeEventMap(byte[] body) {
        try {
            Map<String, Object> message = jsonMapper.readValue(body, Map.class);
            return Mono.just(message);
        } catch (JacksonException e) {
            return Mono.error(e);
        }
    }

}
