package de.bennyboer.kicherkrabbe.inquiries.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.inquiries.InquiriesModule;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(InquiriesMessaging.class)
public class InquiriesMessagingTest extends EventListenerTest {

    @MockBean
    private InquiriesModule module;

    @Autowired
    public InquiriesMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        when(module.allowUserToManageInquiries(any())).thenReturn(Mono.empty());
        when(module.removePermissionsForUser(any())).thenReturn(Mono.empty());
        when(module.updateInquiryInLookup(any())).thenReturn(Mono.empty());
        when(module.removeInquiryFromLookup(any())).thenReturn(Mono.empty());
        when(module.removePermissions(any())).thenReturn(Mono.empty());
        when(module.allowSystemToReadAndDeleteInquiry(any())).thenReturn(Mono.empty());
        when(module.deleteInquiry(any(), any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowUserToManageInquiries() {
        // when: a user created event is published
        send(
                AggregateType.of("USER"),
                AggregateId.of("USER_ID"),
                Version.of(1),
                EventName.of("CREATED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the user is allowed to manage inquiries
        verify(module, timeout(5000).times(1)).allowUserToManageInquiries(eq("USER_ID"));
    }

    @Test
    void shouldRemovePermissionsForUser() {
        // when: a user deleted event is published
        send(
                AggregateType.of("USER"),
                AggregateId.of("USER_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the permissions for the user are removed
        verify(module, timeout(5000).times(1)).removePermissionsForUser(eq("USER_ID"));
    }

    @Test
    void shouldUpdateInquiryLookup() {
        // when: an inquiry sent event is published
        send(
                AggregateType.of("INQUIRY"),
                AggregateId.of("INQUIRY_ID"),
                Version.of(1),
                EventName.of("SENT"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the inquiry is updated in the lookup
        verify(module, timeout(5000).times(1)).updateInquiryInLookup(eq("INQUIRY_ID"));
    }

    @Test
    void shouldRemoveInquiryFromLookup() {
        // when: an inquiry deleted event is published
        send(
                AggregateType.of("INQUIRY"),
                AggregateId.of("INQUIRY_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the inquiry is removed from the lookup
        verify(module, timeout(5000).times(1)).removeInquiryFromLookup(eq("INQUIRY_ID"));
    }

    @Test
    void shouldRemovePermissionsForDeletedInquiry() {
        // when: an inquiry deleted event is published
        send(
                AggregateType.of("INQUIRY"),
                AggregateId.of("INQUIRY_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the permissions for the inquiry are removed
        verify(module, timeout(10000).times(1)).removePermissions(eq("INQUIRY_ID"));
    }

    @Test
    void shouldAllowSystemUserToReadAndDeleteInquiry() {
        // when: an inquiry sent event is published
        send(
                AggregateType.of("INQUIRY"),
                AggregateId.of("INQUIRY_ID"),
                Version.of(1),
                EventName.of("SENT"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of()
        );

        // then: the system user is allowed to read and delete inquiries
        verify(module, timeout(5000).times(1)).allowSystemToReadAndDeleteInquiry(eq("INQUIRY_ID"));
    }

    @Test
    void shouldDeleteInquiryOnMailDeletedIfOriginIsInquiry() {
        // when: a mail deleted event is published
        send(
                AggregateType.of("MAIL"),
                AggregateId.of("MAIL_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "origin", Map.of(
                                "type", "INQUIRY",
                                "id", "SOME_INQUIRY_ID"
                        )
                )
        );

        // then: the inquiry is deleted
        verify(module, timeout(5000).times(1)).deleteInquiry(eq("SOME_INQUIRY_ID"), eq(Agent.system()));
    }

    @Test
    void shouldNotDeleteInquiryOnMailDeletedIfOriginIsNotInquiry() {
        // when: a mail deleted event is published
        send(
                AggregateType.of("MAIL"),
                AggregateId.of("MAIL_ID"),
                Version.of(1),
                EventName.of("DELETED"),
                Version.zero(),
                Agent.system(),
                Instant.now(),
                Map.of(
                        "origin", Map.of(
                                "type", "SOME_TYPE",
                                "id", "SOME_ID"
                        )
                )
        );

        // then: the inquiry is not deleted
        verify(module, timeout(5000).times(0)).deleteInquiry(any(), any());
    }

}
