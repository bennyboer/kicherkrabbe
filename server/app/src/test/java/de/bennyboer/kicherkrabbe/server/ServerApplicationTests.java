package de.bennyboer.kicherkrabbe.server;

import de.bennyboer.kicherkrabbe.persistence.MongoTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(MongoTestConfig.class)
class ServerApplicationTests {

    @Test
    void contextLoads() {
    }

}
