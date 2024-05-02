package de.bennyboer.kicherkrabbe.persistence;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

@Configuration
@EnableReactiveMongoRepositories
@EnableTransactionManagement
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    private final MongoProperties properties;

    public MongoConfig(MongoProperties properties, List<MongoPropertiesCustomizer> customizers) {
        this.properties = properties;

        customizers.forEach(customizer -> customizer.customize(properties));
    }

    @Override
    protected String getDatabaseName() {
        return properties.getMongoClientDatabase();
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        builder.applyConnectionString(new ConnectionString(properties.determineUri()));
    }

    @Bean
    ReactiveMongoTransactionManager transactionManager(ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory) {
        return new ReactiveMongoTransactionManager(reactiveMongoDatabaseFactory);
    }

}
