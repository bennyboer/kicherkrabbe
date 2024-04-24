package de.bennyboer.kicherkrabbe.messaging.testing;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.RabbitMQContainer;

import java.util.Set;

public class RabbitTestSupport {

    private static final RabbitMQContainer CONTAINER;

    static {
        CONTAINER = new RabbitMQContainer("rabbitmq:3.13.1-management")
                .withUser("guest", "guest", Set.of("administrator"))
                .withPermission("/", "guest", ".*", ".*", ".*")
                .withExposedPorts(5672, 15672);

        CONTAINER.start();
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            var values = TestPropertyValues.of(
                    "spring.rabbitmq.username=guest",
                    "spring.rabbitmq.password=guest",
                    "spring.rabbitmq.host=%s".formatted(CONTAINER.getHost()),
                    "spring.rabbitmq.port=%d".formatted(CONTAINER.getMappedPort(5672))
            );

            values.applyTo(applicationContext.getEnvironment());
        }

    }

}
