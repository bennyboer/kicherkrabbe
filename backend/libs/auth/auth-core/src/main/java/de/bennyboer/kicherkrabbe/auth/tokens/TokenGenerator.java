package de.bennyboer.kicherkrabbe.auth.tokens;

import reactor.core.publisher.Mono;

public interface TokenGenerator {

    Mono<Token> generate(TokenPayload content);

}
