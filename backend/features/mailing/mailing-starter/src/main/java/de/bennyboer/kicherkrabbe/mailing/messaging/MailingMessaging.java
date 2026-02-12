package de.bennyboer.kicherkrabbe.mailing.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.mailing.MailingModule;
import de.bennyboer.kicherkrabbe.mailing.api.ReceiverDTO;
import de.bennyboer.kicherkrabbe.mailing.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailing.api.requests.SendMailRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Configuration
public class MailingMessaging {

    @Bean("mailing_onUserCreatedAddPermissionToReadAndManageMailingSettingsMsgListener")
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

    @Bean("mailing_onUserDeletedRemoveMailingPermissionsMsgListener")
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

    @Bean("mailing_onMailingMailSentUpdateMailInLookupMsgListener")
    public EventListener onMailingMailSentUpdateMailInLookupMsgListener(
            EventListenerFactory factory,
            MailingModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailing.mailing-mail-sent-update-mail-in-lookup",
                AggregateType.of("MAILING_MAIL"),
                EventName.of("SENT"),
                (event) -> {
                    String mailId = event.getMetadata().getAggregateId().getValue();

                    return module.updateMailInLookup(mailId);
                }
        );
    }

    @Bean("mailing_onMailingMailDeletedRemoveMailFromLookupMsgListener")
    public EventListener onMailingMailDeletedRemoveMailFromLookupMsgListener(
            EventListenerFactory factory,
            MailingModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailing.mailing-mail-deleted-remove-mail-from-lookup",
                AggregateType.of("MAILING_MAIL"),
                EventName.of("DELETED"),
                (event) -> {
                    String mailId = event.getMetadata().getAggregateId().getValue();

                    return module.removeMailFromLookup(mailId);
                }
        );
    }

    @Bean("mailing_onMailingMailSentAllowSystemUserToDeleteMailingMailMsgListener")
    public EventListener onMailingMailSentAllowSystemUserToDeleteMailingMailMsgListener(
            EventListenerFactory factory,
            MailingModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailing.mailing-mail-sent-allow-system-user-to-delete-mail",
                AggregateType.of("MAILING_MAIL"),
                EventName.of("SENT"),
                (event) -> {
                    String mailId = event.getMetadata().getAggregateId().getValue();

                    return module.allowSystemUserToDeleteMail(mailId);
                }
        );
    }

    @Bean("mailing_onMailingMailDeletedRemovePermissionsMsgListener")
    public EventListener onMailingMailDeletedRemovePermissionsMsgListener(
            EventListenerFactory factory,
            MailingModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailing.mailing-mail-deleted-remove-permissions",
                AggregateType.of("MAILING_MAIL"),
                EventName.of("DELETED"),
                (event) -> {
                    String mailId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForMail(mailId);
                }
        );
    }

    @Bean("mailing_onMailingMailSentAllowUsersThatAreAllowedToReadToReadMailMsgListener")
    public EventListener onMailingMailSentAllowUsersThatAreAllowedToReadToReadMailMsgListener(
            EventListenerFactory factory,
            MailingModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailing.mailing-mail-sent-allow-users-that-are-allowed-to-read-to-read-mail",
                AggregateType.of("MAILING_MAIL"),
                EventName.of("SENT"),
                (event) -> {
                    String mailId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUsersThatAreAllowedToReadMailsToReadMail(mailId);
                }
        );
    }

    @Bean("mailing_onMailingMailSentSendMailViaMailingServiceMsgListener")
    public EventListener onMailingMailSentSendMailViaMailingServiceMsgListener(
            EventListenerFactory factory,
            MailingModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailing.mailing-mail-sent-send-mail-via-mailing-service",
                AggregateType.of("MAILING_MAIL"),
                EventName.of("SENT"),
                (event) -> {
                    String mailId = event.getMetadata().getAggregateId().getValue();

                    return module.sendMailViaMailingService(mailId, Agent.system());
                }
        );
    }

    @Bean("mailing_onNotificationSentSendMailMsgListener")
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

                    Map<String, Object> origin = (Map<String, Object>) payload.get("origin");
                    String originType = (String) origin.get("type");
                    String originId = (String) origin.get("id");

                    var receiver = new ReceiverDTO();
                    receiver.mail = mail;

                    var request = new SendMailRequest();
                    request.sender = new SenderDTO();
                    request.sender.mail = "no-reply@kicherkrabbe.com";
                    request.receivers = Set.of(receiver);
                    request.subject = "System-Benachrichtigung: %s".formatted(title);
                    request.text = message;

                    getOriginUrl(originType, originId).ifPresent(url -> {
                        request.text += ": %s".formatted(url);
                    });

                    return module.sendMail(request, Agent.system()).then();
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
