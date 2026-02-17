package de.bennyboer.kicherkrabbe.telegram.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.telegram.TelegramModule;
import de.bennyboer.kicherkrabbe.telegram.api.requests.SendMessageViaBotRequest;
import de.bennyboer.kicherkrabbe.telegram.settings.BotApiTokenMissingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Configuration
public class TelegramMessaging {

    record NotificationSentEvent(
            List<Map<String, Object>> channels,
            String title,
            String message,
            Origin origin
    ) {
    }

    record Origin(String type, String id) {
    }

    @Bean("telegram_onUserCreatedAddPermissionToReadAndManageTelegramSettingsMsgListener")
    public EventListener onUserCreatedAddPermissionToReadAndManageTelegramSettingsMsgListener(
            EventListenerFactory factory,
            TelegramModule module
    ) {
        return factory.createEventListenerForEvent(
                "telegram.user-created-add-permission-to-read-and-manage-telegram-settings",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToReadAndManageSettings(userId);
                }
        );
    }

    @Bean("telegram_onUserDeletedRemoveTelegramPermissionsMsgListener")
    public EventListener onUserDeletedRemoveTelegramPermissionsMsgListener(
            EventListenerFactory factory,
            TelegramModule module
    ) {
        return factory.createEventListenerForEvent(
                "telegram.user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForUser(userId);
                }
        );
    }

    @Bean("telegram_onNotificationSentSendTelegramMessageViaBotMsgListener")
    public EventListener onNotificationSentSendTelegramMessageViaBotMsgListener(
            EventListenerFactory factory,
            TelegramModule module
    ) {
        return factory.createEventListenerForEvent(
                "telegram.notification-sent-send-message-via-bot",
                AggregateType.of("NOTIFICATION"),
                EventName.of("SENT"),
                NotificationSentEvent.class,
                (metadata, event) -> {
                    Map<String, Object> telegramChannel = event.channels().stream()
                            .filter(channel -> "TELEGRAM".equals(channel.get("type")))
                            .findFirst()
                            .orElse(null);
                    if (telegramChannel == null) {
                        return Mono.empty();
                    }

                    String chatId = (String) ((Map<String, Object>) telegramChannel.get("telegram")).get("chatId");

                    var request = new SendMessageViaBotRequest();
                    request.chatId = chatId;
                    request.text = """
                            <em>System-Benachrichtigung</em>: <strong>%s</strong>
                            %s
                            """.formatted(event.title(), event.message());

                    getOriginUrl(event.origin().type(), event.origin().id()).ifPresent(url -> {
                        request.text += """
                                <a href="%s">Jetzt ansehen</a>
                                """.formatted(url, url);
                    });

                    return module.sendMessageViaBot(request, Agent.system())
                            .onErrorResume(
                                    BotApiTokenMissingException.class,
                                    e -> {
                                        log.info("Bot API token is missing. Cannot send message via bot.");
                                        return Mono.empty();
                                    }
                            );
                }
        );
    }

    private Optional<String> getOriginUrl(String originType, String originId) {
        return switch (originType) {
            case "MAIL" -> Optional.of("https://kicherkrabbe.com/admin/mailbox/%s".formatted(originId));
            default -> Optional.empty();
        };
    }

}
