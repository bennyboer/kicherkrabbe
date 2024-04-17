package de.bennyboer.kicherkrabbe.auth.ports.http.requests;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class UseCredentialsRequest {

    String name;

    String password;

}
