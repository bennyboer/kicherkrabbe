package de.bennyboer.kicherkrabbe.auth.tokens;

import de.bennyboer.kicherkrabbe.commons.UserId;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

public class RefreshTokenService {

    private static final Duration REFRESH_TOKEN_LIFETIME = Duration.ofDays(7);

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

    public Mono<RefreshToken> generate(UserId userId) {
        return Mono.fromCallable(() -> {
            Instant now = clock.instant();
            Instant expiresAt = now.plus(REFRESH_TOKEN_LIFETIME);

            return RefreshToken.of(
                    RefreshTokenId.create(),
                    TokenValue.create(),
                    userId,
                    TokenFamilyId.create(),
                    false,
                    expiresAt,
                    now
            );
        }).flatMap(token -> repo.save(token).thenReturn(token));
    }

    public Mono<RefreshResult> refresh(String refreshTokenValue, TokenGenerator accessTokenGenerator) {
        var tokenValue = TokenValue.of(refreshTokenValue);

        return repo.markAsUsedIfNotAlready(tokenValue)
                .flatMap(marked -> {
                    if (marked) {
                        return repo.findByTokenValue(tokenValue)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid refresh token")))
                                .flatMap(token -> {
                                    if (token.isExpired(clock.instant())) {
                                        return Mono.error(new IllegalArgumentException("Refresh token expired"));
                                    }

                                    return createRotatedToken(token)
                                            .delayUntil(repo::save)
                                            .flatMap(newToken -> generateAccessToken(accessTokenGenerator, token.getUserId())
                                                    .map(accessToken -> RefreshResult.of(accessToken, newToken.getTokenValue().getValue())));
                                });
                    }

                    return repo.findByTokenValue(tokenValue)
                            .<RefreshResult>flatMap(token -> repo.revokeFamily(token.getFamily())
                                    .then(Mono.error(new IllegalArgumentException("Refresh token reuse detected"))))
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid refresh token")));
                });
    }

    public Mono<Void> revokeFamily(String refreshTokenValue) {
        return repo.findByTokenValue(TokenValue.of(refreshTokenValue))
                .flatMap(token -> repo.revokeFamily(token.getFamily()));
    }

    public Mono<Void> revokeByUserId(UserId userId) {
        return repo.revokeByUserId(userId);
    }

    private Mono<RefreshToken> createRotatedToken(RefreshToken oldToken) {
        return Mono.fromCallable(() -> RefreshToken.of(
                RefreshTokenId.create(),
                TokenValue.create(),
                oldToken.getUserId(),
                oldToken.getFamily(),
                false,
                oldToken.getExpiresAt(),
                clock.instant()
        ));
    }

    private Mono<String> generateAccessToken(TokenGenerator tokenGenerator, UserId userId) {
        var owner = Owner.of(OwnerId.of(userId.getValue()));
        var payload = TokenPayload.of(owner);
        
        return tokenGenerator.generate(payload).map(Token::getValue);
    }

}
