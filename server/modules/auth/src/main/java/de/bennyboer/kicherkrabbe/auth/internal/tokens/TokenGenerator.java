package de.bennyboer.kicherkrabbe.auth.internal.tokens;

import reactor.core.publisher.Mono;

public interface TokenGenerator {

    Mono<Token> generate(TokenPayload content);

}
