package de.bennyboer.kicherkrabbe.auth.internal.tokens;

import reactor.core.publisher.Mono;

public interface TokenVerifier {

    Mono<TokenPayload> verify(Token token);

}
