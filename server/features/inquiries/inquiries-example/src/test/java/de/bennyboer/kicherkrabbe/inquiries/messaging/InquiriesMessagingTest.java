package de.bennyboer.kicherkrabbe.inquiries.messaging;

import de.bennyboer.kicherkrabbe.eventsourcing.testing.EventListenerTest;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutbox;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.ReactiveTransactionManager;

@Import(InquiriesMessaging.class)
public class InquiriesMessagingTest extends EventListenerTest {

    @MockBean
    private InquiriesMessaging module;

    @Autowired
    public InquiriesMessagingTest(
            MessagingOutbox outbox,
            ReactiveTransactionManager transactionManager
    ) {
        super(outbox, transactionManager);
    }

    @BeforeEach
    void setup() {
        // TODO
    }

    // TODO Add tests

}
