package de.bennyboer.kicherkrabbe.auth.tokens;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import de.bennyboer.kicherkrabbe.auth.keys.KeyPair;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class JWTTokenGenerator implements TokenGenerator {

    private final Algorithm algorithm;

    public JWTTokenGenerator(KeyPair keyPair) {
        algorithm = Algorithm.ECDSA512(keyPair.getPublicKey(), keyPair.getPrivateKey());
    }

    @Override
    public Mono<Token> generate(TokenPayload payload) {
        return Mono.fromCallable(() -> generateToken(payload));
    }

    private Token generateToken(TokenPayload payload) {
        Instant expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);
        String token = JWT.create()
                .withIssuer("server")
                .withSubject(payload.getOwner().getId().getValue())
                .withExpiresAt(expiresAt)
                .sign(algorithm);

        return Token.of(token);
    }

}
