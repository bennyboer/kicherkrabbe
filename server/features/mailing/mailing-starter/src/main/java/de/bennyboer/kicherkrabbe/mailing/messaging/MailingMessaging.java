package de.bennyboer.kicherkrabbe.mailing.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.mailing.MailingModule;
import de.bennyboer.kicherkrabbe.mailing.api.requests.SendMailRequest;
import de.bennyboer.kicherkrabbe.mailing.settings.MailgunApiTokenMissingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class MailingMessaging {

    @Bean
    public EventListener onUserCreatedAddPermissionToReadAndManageMailingSettingsMsgListener(
            EventListenerFactory factory,
            MailingModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailing.user-created-add-permission-to-read-and-manage-mailing-settings",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToReadAndManageSettings(userId);
                }
        );
    }

    @Bean
    public EventListener onUserDeletedRemoveMailingPermissionsMsgListener(
            EventListenerFactory factory,
            MailingModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailing.user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForUser(userId);
                }
        );
    }

    @Bean
    public EventListener onNotificationSentSendMailMsgListener(
            EventListenerFactory factory,
            MailingModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailing.notification-sent-send-mail",
                AggregateType.of("NOTIFICATION"),
                EventName.of("SENT"),
                (event) -> {
                    Map<String, Object> payload = event.getEvent();
                    List<Map<String, Object>> channels = (List<Map<String, Object>>) payload.get("channels");
                    Map<String, Object> emailChannel = channels.stream()
                            .filter(channel -> "EMAIL".equals(channel.get("type")))
                            .findFirst()
                            .orElse(null);
                    if (emailChannel == null) {
                        return Mono.empty();
                    }

                    String mail = (String) emailChannel.get("mail");
                    String title = (String) payload.get("title");
                    String message = (String) payload.get("message");

                    var request = new SendMailRequest();
                    request.mail = mail;
                    request.subject = "System-Benachrichtigung: %s".formatted(title);
                    request.text = message;

                    return module.sendMail(request, Agent.system())
                            .onErrorResume(
                                    MailgunApiTokenMissingException.class,
                                    e -> {
                                        log.info("Mailgun API token is missing. Cannot send mail.");
                                        return Mono.empty();
                                    }
                            );
                }
        );
    }

}
