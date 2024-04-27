package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.UserLookupRepo;
import de.bennyboer.kicherkrabbe.users.internal.UsersService;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class UsersModule {

    private final UsersService usersService;

    private final UserLookupRepo userLookupRepo;

    public Mono<Void> updateUserInLookup(String userId) {
        return Mono.empty(); // TODO
    }

    public Mono<Void> removeUserFromLookup(String userId) {
        return Mono.empty(); // TODO
    }

}
