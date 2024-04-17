package de.bennyboer.kicherkrabbe.auth.internal.tokens;

import lombok.AllArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.security.core.authority.AuthorityUtils.NO_AUTHORITIES;

@AllArgsConstructor
public class JwtTokenAuthenticationFilter implements WebFilter {

    public static final String HEADER_PREFIX = "Bearer ";

    private final TokenVerifier verifier;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return resolveToken(exchange.getRequest())
                .delayUntil(token -> verifyAndCallNextWithAuthentication(token, exchange, chain))
                .switchIfEmpty(chain.filter(exchange).thenReturn(""))
                .then();
    }

    private Mono<Void> verifyAndCallNextWithAuthentication(
            String token,
            ServerWebExchange exchange,
            WebFilterChain chain
    ) {
        return verifier.verify(Token.of(token))
                .flatMap(payload -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(toAuthentication(
                                token,
                                payload
                        ))));
    }

    private Authentication toAuthentication(String token, TokenPayload payload) {
        User user = new User(payload.getOwner().getId().getValue(), "", NO_AUTHORITIES);

        return new UsernamePasswordAuthenticationToken(user, token, NO_AUTHORITIES);
    }

    private Mono<String> resolveToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(AUTHORIZATION);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(HEADER_PREFIX)) {
            return Mono.just(bearerToken.substring(HEADER_PREFIX.length()))
                    .filter(StringUtils::hasText);
        }

        return Mono.empty();
    }

}