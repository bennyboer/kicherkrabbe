package de.bennyboer.kicherkrabbe.auth.tokens;

import reactor.core.publisher.Mono;

public interface RefreshTokenRepo {

    Mono<Void> save(RefreshToken token);

    Mono<RefreshToken> findByTokenValue(String tokenValue);

    Mono<Void> markAsUsed(RefreshTokenId id);

    Mono<Void> revokeFamily(String family);

    Mono<Void> revokeByUserId(String userId);

}
