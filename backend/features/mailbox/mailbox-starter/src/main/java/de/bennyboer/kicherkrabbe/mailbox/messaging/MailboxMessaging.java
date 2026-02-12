package de.bennyboer.kicherkrabbe.mailbox.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.mailbox.MailboxModule;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.OriginTypeDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.SenderDTO;
import de.bennyboer.kicherkrabbe.mailbox.api.requests.ReceiveMailRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class MailboxMessaging {

    @Bean("mailbox_onUserCreatedAddPermissionToReadAndManageMailsMsgListener")
    public EventListener onUserCreatedAddPermissionToReadAndManageMailsMsgListener(
            EventListenerFactory factory,
            MailboxModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailbox.user-created-add-permission-to-read-and-manage-mails",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToReadAndManageMails(userId);
                }
        );
    }

    @Bean("mailbox_onUserDeletedRemoveMailPermissionsMsgListener")
    public EventListener onUserDeletedRemoveMailPermissionsMsgListener(
            EventListenerFactory factory,
            MailboxModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailbox.user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForUser(userId);
                }
        );
    }

    @Bean("mailbox_onMailReceivedUpdateLookupMsgListener")
    public EventListener onMailReceivedUpdateLookupMsgListener(
            EventListenerFactory factory,
            MailboxModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailbox.mail-received-update-lookup",
                AggregateType.of("MAIL"),
                EventName.of("RECEIVED"),
                (event) -> {
                    String mailId = event.getMetadata().getAggregateId().getValue();

                    return module.updateMailInLookup(mailId);
                }
        );
    }

    @Bean("mailbox_onMailMarkedAsReadUpdateLookupMsgListener")
    public EventListener onMailMarkedAsReadUpdateLookupMsgListener(
            EventListenerFactory factory,
            MailboxModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailbox.mail-marked-as-read-update-lookup",
                AggregateType.of("MAIL"),
                EventName.of("MARKED_AS_READ"),
                (event) -> {
                    String mailId = event.getMetadata().getAggregateId().getValue();

                    return module.updateMailInLookup(mailId);
                }
        );
    }

    @Bean("mailbox_onMailMarkedAsUnreadUpdateLookupMsgListener")
    public EventListener onMailMarkedAsUnreadUpdateLookupMsgListener(
            EventListenerFactory factory,
            MailboxModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailbox.mail-marked-as-unread-update-lookup",
                AggregateType.of("MAIL"),
                EventName.of("MARKED_AS_UNREAD"),
                (event) -> {
                    String mailId = event.getMetadata().getAggregateId().getValue();

                    return module.updateMailInLookup(mailId);
                }
        );
    }

    @Bean("mailbox_onMailDeletedRemoveFromLookupMsgListener")
    public EventListener onMailDeletedRemoveFromLookupMsgListener(
            EventListenerFactory factory,
            MailboxModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailbox.mail-deleted-remove-from-lookup",
                AggregateType.of("MAIL"),
                EventName.of("DELETED"),
                (event) -> {
                    String mailId = event.getMetadata().getAggregateId().getValue();

                    return module.removeMailFromLookup(mailId);
                }
        );
    }

    @Bean("mailbox_onMailDeletedRemovePermissionsMsgListener")
    public EventListener onMailDeletedRemovePermissionsMsgListener(
            EventListenerFactory factory,
            MailboxModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailbox.mail-deleted-remove-permissions",
                AggregateType.of("MAIL"),
                EventName.of("DELETED"),
                (event) -> {
                    String mailId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForMail(mailId);
                }
        );
    }

    @Bean("mailbox_onMailReceivedAllowUsersToManageMailMsgListener")
    public EventListener onMailReceivedAllowUsersToManageMailMsgListener(
            EventListenerFactory factory,
            MailboxModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailbox.mail-received-allow-users-to-manage-mail",
                AggregateType.of("MAIL"),
                EventName.of("RECEIVED"),
                (event) -> {
                    String mailId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUsersThatAreAllowedToManageMailsToManageMail(mailId);
                }
        );
    }

    @Bean("mailbox_onInquirySentReceiveMailMsgListener")
    public EventListener onInquirySentReceiveMailMsgListener(
            EventListenerFactory factory,
            MailboxModule module
    ) {
        return factory.createEventListenerForEvent(
                "mailbox.inquiry-sent-receive-mail",
                AggregateType.of("INQUIRY"),
                EventName.of("SENT"),
                (event) -> {
                    String inquiryId = event.getMetadata().getAggregateId().getValue();

                    Map<String, Object> payload = event.getEvent();
                    Map<String, Object> senderPayload = (Map<String, Object>) payload.get("sender");
                    String senderName = (String) senderPayload.get("name");
                    String senderMail = (String) senderPayload.get("mail");
                    String senderPhone = (String) senderPayload.get("phone");
                    String subject = (String) payload.get("subject");
                    String content = (String) payload.get("message");

                    var origin = new OriginDTO();
                    origin.type = OriginTypeDTO.INQUIRY;
                    origin.id = inquiryId;

                    var sender = new SenderDTO();
                    sender.name = senderName;
                    sender.mail = senderMail;
                    sender.phone = senderPhone;

                    var receiveRequest = new ReceiveMailRequest();
                    receiveRequest.origin = origin;
                    receiveRequest.sender = sender;
                    receiveRequest.subject = subject;
                    receiveRequest.content = content;

                    return module.receiveMail(receiveRequest, Agent.system()).then();
                }
        );
    }

}
