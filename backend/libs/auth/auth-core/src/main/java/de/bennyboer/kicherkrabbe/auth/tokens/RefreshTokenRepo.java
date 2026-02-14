package de.bennyboer.kicherkrabbe.auth.tokens;

import de.bennyboer.kicherkrabbe.commons.UserId;
import reactor.core.publisher.Mono;

public interface RefreshTokenRepo {

    Mono<Void> save(RefreshToken token);

    Mono<RefreshToken> findByTokenValue(TokenValue tokenValue);

    Mono<Boolean> markAsUsedIfNotAlready(TokenValue tokenValue);

    Mono<Void> revokeFamily(TokenFamilyId family);

    Mono<Void> revokeByUserId(UserId userId);

}
