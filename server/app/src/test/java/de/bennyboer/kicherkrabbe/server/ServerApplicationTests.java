package de.bennyboer.kicherkrabbe.server;

import de.bennyboer.kicherkrabbe.messaging.testing.MessagingTestConfig;
import de.bennyboer.kicherkrabbe.persistence.MongoTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Import({MongoTestConfig.class, MessagingTestConfig.class})
class ServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
