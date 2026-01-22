package de.bennyboer.kicherkrabbe.messaging;

import de.bennyboer.kicherkrabbe.messaging.inbox.MessagingInbox;
import de.bennyboer.kicherkrabbe.messaging.inbox.MessagingInboxConfig;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListenerFactory;
import de.bennyboer.kicherkrabbe.messaging.outbox.MessagingOutboxConfig;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.amqp.autoconfigure.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.ReactiveTransactionManager;
import tools.jackson.databind.json.JsonMapper;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Configuration
@Import({
        MessagingInboxConfig.class,
        MessagingOutboxConfig.class
})
public class MessagingConfig {

    @Bean("messagingJsonMapper")
    @ConditionalOnMissingBean(name = "messagingJsonMapper")
    public JsonMapper messagingJsonMapper() {
        return JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(NON_NULL))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(ConnectionFactory.class)
    public ConnectionFactory connectionFactory(RabbitProperties rabbitProperties) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();

        connectionFactory.setHost(rabbitProperties.determineHost());
        connectionFactory.setPort(rabbitProperties.determinePort());
        connectionFactory.setUsername(rabbitProperties.determineUsername());
        connectionFactory.setPassword(rabbitProperties.determinePassword());
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);

        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public MessageListenerFactory messageListenerFactory(
            ConnectionFactory connectionFactory,
            RabbitAdmin rabbitAdmin,
            ReactiveTransactionManager transactionManager,
            MessagingInbox inbox
    ) {
        return new MessageListenerFactory(connectionFactory, rabbitAdmin, transactionManager, inbox);
    }

}
