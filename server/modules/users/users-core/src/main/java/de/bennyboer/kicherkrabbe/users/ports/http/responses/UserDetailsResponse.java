package de.bennyboer.kicherkrabbe.users.ports.http.responses;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class UserDetailsResponse {

    String userId;

    String firstName;

    String lastName;

    String mail;

}
