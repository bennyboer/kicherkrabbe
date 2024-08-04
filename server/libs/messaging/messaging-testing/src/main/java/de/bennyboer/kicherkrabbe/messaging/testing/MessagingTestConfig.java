package de.bennyboer.kicherkrabbe.messaging.testing;

import com.rabbitmq.client.ConnectionFactory;
import de.bennyboer.kicherkrabbe.messaging.MessagingConfig;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.MessagingInboxRepo;
import de.bennyboer.kicherkrabbe.messaging.inbox.persistence.inmemory.InMemoryMessagingInboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.MessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.messaging.outbox.persistence.inmemory.InMemoryMessagingOutboxRepo;
import de.bennyboer.kicherkrabbe.persistence.MockReactiveTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.ReactiveTransactionManager;
import org.testcontainers.containers.RabbitMQContainer;

import java.time.Duration;

@Configuration
@Import(MessagingConfig.class)
public class MessagingTestConfig {

    @Bean
    public MessagingInboxRepo messagingInboxRepo() {
        return new InMemoryMessagingInboxRepo(false);
    }

    @Bean
    public MessagingOutboxRepo messagingOutboxRepo() {
        return new InMemoryMessagingOutboxRepo();
    }

    @Bean
    public ReactiveTransactionManager transactionManager() {
        return new MockReactiveTransactionManager();
    }

    @Bean
    public ConnectionFactory reactorRabbitConnectionFactory(RabbitMQContainer container) {
        int mappedPort = container.getMappedPort(5672);
        String host = container.getHost();
        String username = container.getAdminUsername();
        String password = container.getAdminPassword();

        ConnectionFactory connectionFactory = new ConnectionFactory();

        connectionFactory.useNio();
        connectionFactory.setHost(host);
        connectionFactory.setPort(mappedPort);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);

        return connectionFactory;
    }

    @Bean(destroyMethod = "stop")
    public RabbitMQContainer rabbitMQContainer() {
        var container = new RabbitMQContainer("rabbitmq:3.13.4-management")
                .withExposedPorts(5672, 15672)
                .withStartupTimeout(Duration.ofSeconds(120));

        container.start();

        return container;
    }

}
