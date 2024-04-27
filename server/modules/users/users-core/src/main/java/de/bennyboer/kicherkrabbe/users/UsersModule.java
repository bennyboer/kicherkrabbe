package de.bennyboer.kicherkrabbe.users;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.UserLookup;
import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.UserLookupRepo;
import de.bennyboer.kicherkrabbe.users.internal.*;
import de.bennyboer.kicherkrabbe.users.internal.create.MailAlreadyInUseError;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
public class UsersModule {

    private final UsersService usersService;

    private final UserLookupRepo userLookupRepo;

    private final ReactiveTransactionManager transactionManager;

    @PostConstruct
    public void init() {
        createDefaultUserIfNoneExists()
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    @Transactional
    public Mono<String> createUser(String firstName, String lastName, String mail) {
        var name = FullName.of(
                FirstName.of(firstName),
                LastName.of(lastName)
        );

        // TODO Check permissions
        return assertThatMailNotAlreadyInUse(Mail.of(mail))
                .then(usersService.create(
                        name,
                        Mail.of(mail),
                        Agent.system()
                ))
                .map(result -> result.getId().getValue());
    }

    @Transactional
    public Mono<Void> deleteUser(String userId) {
        // TODO Check permissions
        return usersService.delete(UserId.of(userId), Agent.system()).then();
    }

    @Transactional
    public Mono<Void> renameUser(String userId, String firstName, String lastName) {
        var name = FullName.of(
                FirstName.of(firstName),
                LastName.of(lastName)
        );

        // TODO Check permissions
        return usersService.rename(
                UserId.of(userId),
                name,
                Agent.system()
        ).then();
    }

    public Mono<UserDetails> getUserDetails(String userId) {
        // TODO Check permissions
        return usersService.get(UserId.of(userId))
                .map(user -> UserDetails.of(user.getId(), user.getName(), user.getMail()));
    }

    public Mono<Void> updateUserInLookup(String userId) {
        return usersService.get(UserId.of(userId))
                .map(user -> UserLookup.of(user.getId(), user.getName(), user.getMail()))
                .flatMap(userLookupRepo::update);
    }

    public Mono<Void> removeUserFromLookup(String userId) {
        return userLookupRepo.remove(UserId.of(userId));
    }

    private Mono<Void> assertThatMailNotAlreadyInUse(Mail mail) {
        return userLookupRepo.findByMail(mail)
                .flatMap(user -> Mono.error(new MailAlreadyInUseError(mail.getValue())));
    }

    private Mono<Void> createDefaultUserIfNoneExists() {
        TransactionalOperator transactionalOperator = TransactionalOperator.create(transactionManager);

        // TODO Configure default user via configuration file
        return userLookupRepo.count()
                .filter(count -> count == 0)
                .flatMap(count -> createUser("Default", "User", "default@kicherkrabbe.com"))
                .as(transactionalOperator::transactional)
                .then();
    }

}
