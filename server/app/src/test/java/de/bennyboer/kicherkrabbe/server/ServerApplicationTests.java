package de.bennyboer.kicherkrabbe.server;

import de.bennyboer.kicherkrabbe.testing.persistence.MongoTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(initializers = {MongoTestSupport.Initializer.class})
class ServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
