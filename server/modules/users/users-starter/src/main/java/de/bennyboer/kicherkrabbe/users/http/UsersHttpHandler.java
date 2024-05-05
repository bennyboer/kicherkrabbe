package de.bennyboer.kicherkrabbe.users.http;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.users.UsersModule;
import de.bennyboer.kicherkrabbe.users.http.responses.UserDetailsResponse;
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
                .flatMap(userId -> module.getUserDetails(userId, Agent.user(AgentId.of(userId))))
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
