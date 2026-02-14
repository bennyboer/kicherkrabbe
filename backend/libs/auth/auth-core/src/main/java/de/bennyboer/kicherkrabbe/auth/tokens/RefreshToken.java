package de.bennyboer.kicherkrabbe.auth.tokens;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RefreshToken {

    RefreshTokenId id;

    String tokenValue;

    String userId;

    String family;

    boolean used;

    Instant expiresAt;

    Instant createdAt;

    public static RefreshToken of(
            RefreshTokenId id,
            String tokenValue,
            String userId,
            String family,
            boolean used,
            Instant expiresAt,
            Instant createdAt
    ) {
        notNull(id, "Refresh token ID must be given");
        notNull(tokenValue, "Token value must be given");
        check(!tokenValue.isBlank(), "Token value must not be blank");
        notNull(userId, "User ID must be given");
        check(!userId.isBlank(), "User ID must not be blank");
        notNull(family, "Family must be given");
        check(!family.isBlank(), "Family must not be blank");
        notNull(expiresAt, "Expiry must be given");
        notNull(createdAt, "Creation time must be given");

        return new RefreshToken(id, tokenValue, userId, family, used, expiresAt, createdAt);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

}
