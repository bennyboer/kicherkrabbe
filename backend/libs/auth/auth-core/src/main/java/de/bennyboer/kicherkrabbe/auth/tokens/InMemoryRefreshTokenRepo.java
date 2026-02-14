package de.bennyboer.kicherkrabbe.auth.tokens;

import de.bennyboer.kicherkrabbe.commons.UserId;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRefreshTokenRepo implements RefreshTokenRepo {

    private final Map<TokenValue, RefreshToken> tokensByValue = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> save(RefreshToken token) {
        return Mono.fromRunnable(() -> tokensByValue.put(token.getTokenValue(), token));
    }

    @Override
    public Mono<RefreshToken> findByTokenValue(TokenValue tokenValue) {
        return Mono.justOrEmpty(tokensByValue.get(tokenValue));
    }

    @Override
    public Mono<Boolean> markAsUsedIfNotAlready(TokenValue tokenValue) {
        return Mono.fromCallable(() -> {
            var existing = tokensByValue.get(tokenValue);
            if (existing == null || existing.isUsed()) {
                return false;
            }

            var updated = RefreshToken.of(
                    existing.getId(),
                    existing.getTokenValue(),
                    existing.getUserId(),
                    existing.getFamily(),
                    true,
                    existing.getExpiresAt(),
                    existing.getCreatedAt()
            );

            return tokensByValue.replace(tokenValue, existing, updated);
        });
    }

    @Override
    public Mono<Void> revokeFamily(TokenFamilyId family) {
        return Mono.fromRunnable(() ->
                tokensByValue.entrySet().removeIf(entry -> entry.getValue().getFamily().equals(family)));
    }

    @Override
    public Mono<Void> revokeByUserId(UserId userId) {
        return Mono.fromRunnable(() ->
                tokensByValue.entrySet().removeIf(entry -> entry.getValue().getUserId().equals(userId)));
    }

}
