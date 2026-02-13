package de.bennyboer.kicherkrabbe.auth.tokens;

import reactor.core.publisher.Mono;

public interface RefreshTokenRepo {

    Mono<Void> save(RefreshToken token);

    Mono<RefreshToken> findByTokenValue(String tokenValue);

    Mono<Boolean> markAsUsedIfNotAlready(String tokenValue);

    Mono<Void> revokeFamily(String family);

    Mono<Void> revokeByUserId(String userId);

}
