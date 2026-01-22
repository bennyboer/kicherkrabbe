package de.bennyboer.kicherkrabbe.permissions;

import de.bennyboer.kicherkrabbe.messaging.listener.MessageListenerFactory;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import de.bennyboer.kicherkrabbe.permissions.events.MessagingPermissionsEventPublisher;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionEventListenerFactory;
import de.bennyboer.kicherkrabbe.permissions.events.PermissionsEventPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.util.Optional;

@Configuration
public class PermissionsConfig {

    @Bean
    @ConditionalOnMissingBean(PermissionsEventPublisher.class)
    public PermissionsEventPublisher permissionsEventPublisher(MessagingOutbox outbox, Optional<Clock> clock) {
        return new MessagingPermissionsEventPublisher(outbox, clock.orElseGet(Clock::systemUTC));
    }

    @Bean
    public PermissionEventListenerFactory permissionEventListenerFactory(
            MessageListenerFactory messageListenerFactory,
            Optional<JsonMapper> jsonMapper
    ) {
        return new PermissionEventListenerFactory(messageListenerFactory, jsonMapper.orElseGet(() -> JsonMapper.builder().build()));
    }

}
