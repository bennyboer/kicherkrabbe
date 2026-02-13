package de.bennyboer.kicherkrabbe.credentials.http.api.responses;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class RefreshTokenResponse {

    String token;

    String refreshToken;

}
