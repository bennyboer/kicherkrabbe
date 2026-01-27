package de.bennyboer.kicherkrabbe.persistence;

import org.springframework.boot.mongodb.autoconfigure.MongoProperties;

public interface MongoPropertiesCustomizer {

    void customize(MongoProperties properties);

}
