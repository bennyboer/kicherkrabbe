package de.bennyboer.kicherkrabbe.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import de.bennyboer.kicherkrabbe.messaging.listener.MessageListener;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.rabbitmq.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@Configuration
public class MessagingConfig {

    @Bean("messagingObjectMapper")
    @ConditionalOnMissingBean(name = "messagingObjectMapper")
    public ObjectMapper messagingObjectMapper() {
        return new ObjectMapper()
                .setDefaultPropertyInclusion(NON_NULL)
                .registerModule(new JavaTimeModule())
                .disable(WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    @ConditionalOnMissingBean(ConnectionFactory.class)
    public ConnectionFactory reactorRabbitConnectionFactory(RabbitProperties rabbitProperties) {
        ConnectionFactory connectionFactory = new ConnectionFactory();

        connectionFactory.useNio();
        connectionFactory.setHost(rabbitProperties.determineHost());
        connectionFactory.setPort(rabbitProperties.determinePort());
        connectionFactory.setUsername(rabbitProperties.determineUsername());
        connectionFactory.setPassword(rabbitProperties.determinePassword());

        return connectionFactory;
    }

    @Bean(destroyMethod = "destroy")
    public ConnectionContainer connectionContainer(ConnectionFactory connectionFactory) {
        Mono<Connection> connectionMono = Mono.fromCallable(
                () -> connectionFactory.newConnection("kicherkrabbe-server")
        ).cache();

        return new ConnectionContainer(connectionMono);
    }

    @Bean
    public SenderOptions senderOptions(ConnectionContainer connectionContainer) {
        return new SenderOptions()
                .connectionMono(connectionContainer.getConnectionMono())
                .resourceManagementScheduler(Schedulers.boundedElastic());
    }

    @Bean
    public Sender sender(SenderOptions options) {
        return RabbitFlux.createSender(options);
    }

    @Bean
    public ReceiverOptions receiverOptions(ConnectionContainer connectionContainer) {
        return new ReceiverOptions()
                .connectionMono(connectionContainer.getConnectionMono());
    }

    @Bean
    public Receiver receiver(ReceiverOptions options) {
        return RabbitFlux.createReceiver(options);
    }
    
    @Bean
    public MessageListener messageListener(Sender sender, Receiver receiver) {
        return new MessageListener(sender, receiver);
    }

}
