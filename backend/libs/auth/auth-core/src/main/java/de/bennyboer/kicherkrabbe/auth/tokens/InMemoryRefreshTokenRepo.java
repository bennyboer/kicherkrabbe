package de.bennyboer.kicherkrabbe.auth.tokens;

import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRefreshTokenRepo implements RefreshTokenRepo {

    private final Map<String, RefreshToken> tokensByValue = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> save(RefreshToken token) {
        return Mono.fromRunnable(() -> tokensByValue.put(token.getTokenValue(), token));
    }

    @Override
    public Mono<RefreshToken> findByTokenValue(String tokenValue) {
        return Mono.justOrEmpty(tokensByValue.get(tokenValue));
    }

    @Override
    public Mono<Void> markAsUsed(RefreshTokenId id) {
        return Mono.fromRunnable(() -> tokensByValue.replaceAll((key, token) -> {
            if (token.getId().equals(id)) {
                return RefreshToken.of(
                        token.getId(),
                        token.getTokenValue(),
                        token.getUserId(),
                        token.getFamily(),
                        true,
                        token.getExpiresAt(),
                        token.getCreatedAt()
                );
            }
            return token;
        }));
    }

    @Override
    public Mono<Void> revokeFamily(String family) {
        return Mono.fromRunnable(() ->
                tokensByValue.entrySet().removeIf(entry -> entry.getValue().getFamily().equals(family)));
    }

    @Override
    public Mono<Void> revokeByUserId(String userId) {
        return Mono.fromRunnable(() ->
                tokensByValue.entrySet().removeIf(entry -> entry.getValue().getUserId().equals(userId)));
    }

}
