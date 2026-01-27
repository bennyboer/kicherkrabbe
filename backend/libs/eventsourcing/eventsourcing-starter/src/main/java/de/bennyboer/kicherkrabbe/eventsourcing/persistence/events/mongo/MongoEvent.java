package de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.mongo;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.Map;

@Document
public class MongoEvent {

    @MongoId
    public String id;

    public MongoAggregate aggregate;

    public MongoAgent agent;

    public Instant date;

    public String name;

    public long version;

    public boolean snapshot;

    public Map<String, Object> payload;

}
