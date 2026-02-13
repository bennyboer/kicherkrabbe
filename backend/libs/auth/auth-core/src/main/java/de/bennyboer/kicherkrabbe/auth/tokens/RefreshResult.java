package de.bennyboer.kicherkrabbe.auth.tokens;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RefreshResult {

    String accessToken;

    String refreshToken;

    public static RefreshResult of(String accessToken, String refreshToken) {
        notNull(accessToken, "Access token must be given");
        check(!accessToken.isBlank(), "Access token must not be blank");
        notNull(refreshToken, "Refresh token must be given");
        check(!refreshToken.isBlank(), "Refresh token must not be blank");

        return new RefreshResult(accessToken, refreshToken);
    }

}
