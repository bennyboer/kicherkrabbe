package de.bennyboer.kicherkrabbe.auth.tokens;

import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

public class RefreshTokenService {

    private static final Duration REFRESH_TOKEN_LIFETIME = Duration.ofDays(7);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepo repo;
    private final Clock clock;

    public RefreshTokenService(RefreshTokenRepo repo, Clock clock) {
        notNull(repo, "Refresh token repo must be given");
        notNull(clock, "Clock must be given");

        this.repo = repo;
        this.clock = clock;
    }

    public RefreshTokenService(RefreshTokenRepo repo) {
        this(repo, Clock.systemUTC());
    }

    public Mono<RefreshToken> generate(String userId) {
        return Mono.fromCallable(() -> {
            Instant now = clock.instant();
            Instant expiresAt = now.plus(REFRESH_TOKEN_LIFETIME);
            String family = UUID.randomUUID().toString();

            return RefreshToken.of(
                    RefreshTokenId.create(),
                    generateTokenValue(),
                    userId,
                    family,
                    false,
                    expiresAt,
                    now
            );
        }).flatMap(token -> repo.save(token).thenReturn(token));
    }

    public Mono<RefreshResult> refresh(String refreshTokenValue, TokenGenerator accessTokenGenerator) {
        return repo.markAsUsedIfNotAlready(refreshTokenValue)
                .flatMap(marked -> {
                    if (marked) {
                        return repo.findByTokenValue(refreshTokenValue)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid refresh token")))
                                .flatMap(token -> {
                                    if (token.isExpired(clock.instant())) {
                                        return Mono.error(new IllegalArgumentException("Refresh token expired"));
                                    }

                                    return createRotatedToken(token)
                                            .flatMap(newToken -> repo.save(newToken).thenReturn(newToken))
                                            .flatMap(newToken -> generateAccessToken(accessTokenGenerator, token.getUserId())
                                                    .map(accessToken -> RefreshResult.of(accessToken, newToken.getTokenValue())));
                                });
                    }

                    return repo.findByTokenValue(refreshTokenValue)
                            .<RefreshResult>flatMap(token -> repo.revokeFamily(token.getFamily())
                                    .then(Mono.error(new IllegalArgumentException("Refresh token reuse detected"))))
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid refresh token")));
                });
    }

    public Mono<Void> revokeFamily(String refreshTokenValue) {
        return repo.findByTokenValue(refreshTokenValue)
                .flatMap(token -> repo.revokeFamily(token.getFamily()));
    }

    public Mono<Void> revokeByUserId(String userId) {
        return repo.revokeByUserId(userId);
    }

    private Mono<RefreshToken> createRotatedToken(RefreshToken oldToken) {
        return Mono.fromCallable(() -> RefreshToken.of(
                RefreshTokenId.create(),
                generateTokenValue(),
                oldToken.getUserId(),
                oldToken.getFamily(),
                false,
                oldToken.getExpiresAt(),
                clock.instant()
        ));
    }

    private Mono<String> generateAccessToken(TokenGenerator tokenGenerator, String userId) {
        Owner owner = Owner.of(OwnerId.of(userId));
        TokenPayload payload = TokenPayload.of(owner);
        return tokenGenerator.generate(payload).map(Token::getValue);
    }

    private static String generateTokenValue() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
