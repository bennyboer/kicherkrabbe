package de.bennyboer.kicherkrabbe.inquiries.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListener;
import de.bennyboer.kicherkrabbe.eventsourcing.event.listener.EventListenerFactory;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.inquiries.InquiriesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class InquiriesMessaging {

    record MailDeletedEvent(Origin origin) {
    }

    record Origin(String type, String id) {
    }

    @Bean("inquiries_onUserCreatedAddPermissionToManageInquiriesMsgListener")
    public EventListener onUserCreatedAddPermissionToManageInquiriesMsgListener(
            EventListenerFactory factory,
            InquiriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "inquiries.user-created-add-permission-to-manage-inquiries",
                AggregateType.of("USER"),
                EventName.of("CREATED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.allowUserToManageInquiries(userId);
                }
        );
    }

    @Bean("inquiries_onUserDeletedRemoveInquiriesPermissionsMsgListener")
    public EventListener onUserDeletedRemoveInquiriesPermissionsMsgListener(
            EventListenerFactory factory,
            InquiriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "inquiries.user-deleted-remove-permissions",
                AggregateType.of("USER"),
                EventName.of("DELETED"),
                (event) -> {
                    String userId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissionsForUser(userId);
                }
        );
    }

    @Bean("inquiries_onInquirySentUpdateLookupMsgListener")
    public EventListener onInquirySentUpdateLookupMsgListener(
            EventListenerFactory factory,
            InquiriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "inquiries.inquiry-sent-update-lookup",
                AggregateType.of("INQUIRY"),
                EventName.of("SENT"),
                (event) -> {
                    String inquiryId = event.getMetadata().getAggregateId().getValue();

                    return module.updateInquiryInLookup(inquiryId);
                }
        );
    }

    @Bean("inquiries_onInquiryDeletedRemoveFromLookupMsgListener")
    public EventListener onInquiryDeletedRemoveFromLookupMsgListener(
            EventListenerFactory factory,
            InquiriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "inquiries.inquiry-deleted-remove-from-lookup",
                AggregateType.of("INQUIRY"),
                EventName.of("DELETED"),
                (event) -> {
                    String inquiryId = event.getMetadata().getAggregateId().getValue();

                    return module.removeInquiryFromLookup(inquiryId);
                }
        );
    }

    @Bean("inquiries_onInquiryDeletedRemovePermissionsMsgListener")
    public EventListener onInquiryDeletedRemovePermissionsMsgListener(
            EventListenerFactory factory,
            InquiriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "inquiries.inquiry-deleted-remove-permissions",
                AggregateType.of("INQUIRY"),
                EventName.of("DELETED"),
                (event) -> {
                    String inquiryId = event.getMetadata().getAggregateId().getValue();

                    return module.removePermissions(inquiryId);
                }
        );
    }

    @Bean("inquiries_onInquirySentAllowSystemUserToReadAndDeleteMsgListener")
    public EventListener onInquirySentAllowSystemUserToReadAndDeleteMsgListener(
            EventListenerFactory factory,
            InquiriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "inquiries.inquiry-sent-allow-system-user-to-manage-inquiry",
                AggregateType.of("INQUIRY"),
                EventName.of("SENT"),
                (event) -> {
                    String inquiryId = event.getMetadata().getAggregateId().getValue();

                    return module.allowSystemToReadAndDeleteInquiry(inquiryId);
                }
        );
    }

    @Bean("inquiries_onMailDeletedDeleteInquiryMsgListener")
    public EventListener onMailDeletedDeleteInquiryMsgListener(
            EventListenerFactory factory,
            InquiriesModule module
    ) {
        return factory.createEventListenerForEvent(
                "inquiries.mail-deleted-delete-inquiry",
                AggregateType.of("MAIL"),
                EventName.of("DELETED"),
                MailDeletedEvent.class,
                (metadata, event) -> {
                    var origin = event.origin();
                    if (origin == null || !"INQUIRY".equals(origin.type())) {
                        return Mono.empty();
                    }

                    return module.deleteInquiry(origin.id(), Agent.system());
                }
        );
    }

}
