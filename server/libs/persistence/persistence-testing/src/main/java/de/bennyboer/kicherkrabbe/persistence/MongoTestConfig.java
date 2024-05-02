package de.bennyboer.kicherkrabbe.persistence;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;

@Configuration
@Import(MongoConfig.class)
public class MongoTestConfig {

    @Bean(destroyMethod = "stop")
    public MongoDBContainer mongoDBContainer() {
        var container = new MongoDBContainer("mongo:latest");

        container.start();

        return container;
    }

    @Bean
    public MongoPropertiesCustomizer testMongoPropertiesCustomizer(MongoDBContainer container) {
        return properties -> properties.setUri(container.getReplicaSetUrl());
    }

}
