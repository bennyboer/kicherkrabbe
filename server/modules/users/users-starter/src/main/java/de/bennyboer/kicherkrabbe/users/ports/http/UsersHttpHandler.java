package de.bennyboer.kicherkrabbe.users.ports.http;

import de.bennyboer.kicherkrabbe.users.UsersModule;
import de.bennyboer.kicherkrabbe.users.ports.http.responses.UserDetailsResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.security.Principal;

@AllArgsConstructor
public class UsersHttpHandler {

    private final UsersModule module;

    public Mono<ServerResponse> getLoggedInUserDetails(ServerRequest request) {
        var userId$ = request.principal()
                .map(Principal::getName);

        return userId$
                .flatMap(module::getUserDetails)
                .map(details -> {
                    var result = new UserDetailsResponse();

                    result.userId = details.getUserId().getValue();
                    result.firstName = details.getName().getFirstName().getValue();
                    result.lastName = details.getName().getLastName().getValue();
                    result.mail = details.getMail().getValue();

                    return result;
                })
                .flatMap(user -> ServerResponse.ok().bodyValue(user));
    }

}
