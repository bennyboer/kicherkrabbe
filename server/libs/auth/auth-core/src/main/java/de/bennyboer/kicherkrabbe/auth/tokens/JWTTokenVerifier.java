package de.bennyboer.kicherkrabbe.auth.tokens;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.bennyboer.kicherkrabbe.auth.keys.KeyPair;
import reactor.core.publisher.Mono;

public class JWTTokenVerifier implements TokenVerifier {

    private final JWTVerifier verifier;

    public JWTTokenVerifier(KeyPair keyPair) {
        Algorithm algorithm = Algorithm.ECDSA512(keyPair.getPublicKey(), keyPair.getPrivateKey());
        verifier = JWT.require(algorithm)
                .withIssuer("server")
                .build();
    }

    @Override
    public Mono<TokenPayload> verify(Token token) {
        return Mono.fromCallable(() -> verifier.verify(token.getValue()))
                .map(this::extractTokenPayload);
    }

    private TokenPayload extractTokenPayload(DecodedJWT jwt) {
        Owner owner = getTokenOwner(jwt);
        return TokenPayload.of(owner);
    }

    private Owner getTokenOwner(DecodedJWT jwt) {
        boolean isSystem = jwt.getSubject().equals(Owner.system().getId().getValue());
        if (isSystem) {
            return Owner.system();
        }

        OwnerId ownerId = OwnerId.of(jwt.getSubject());
        return Owner.of(ownerId);
    }

}
