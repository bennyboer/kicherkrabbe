package de.bennyboer.kicherkrabbe.persistence;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;

public interface MongoPropertiesCustomizer {

    void customize(MongoProperties properties);

}
