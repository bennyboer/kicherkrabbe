package de.bennyboer.kicherkrabbe.testing.persistence;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MongoDBContainer;

public class MongoTestSupport {

    private static final MongoDBContainer CONTAINER;

    static {
        CONTAINER = new MongoDBContainer("mongo:latest");
        CONTAINER.start();
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            var values = TestPropertyValues.of(
                    "spring.data.mongodb.uri=" + CONTAINER.getReplicaSetUrl()
            );

            values.applyTo(applicationContext.getEnvironment());
        }

    }

}
