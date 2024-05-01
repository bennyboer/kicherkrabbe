package de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.mongo;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.MongoId;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class MongoUserLookup {

    @MongoId
    String userId;

    String firstName;

    String lastName;

    String mail;

}