package de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup;

import de.bennyboer.kicherkrabbe.users.internal.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class UserLookupRepoTest {

    private UserLookupRepo repo;

    protected abstract UserLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldUpdateUserInLookup() {
        // given: a user to update in the lookup
        var userLookup = UserLookup.of(
                UserId.of("USER_ID"),
                FullName.of(
                        FirstName.of("Max"),
                        LastName.of("Mustermann")
                ),
                Mail.of("max.mustermann@kicherkrabbe.com")
        );

        // when: the user is updated in the lookup
        update(userLookup);

        // then: the user can be found by its mail
        var foundUser = findByMail(userLookup.getMail());
        assertThat(foundUser).isEqualTo(userLookup);
    }

    @Test
    void shouldRemoveUserFromLookup() {
        // given: a user in the lookup
        var userLookup = UserLookup.of(
                UserId.of("USER_ID"),
                FullName.of(
                        FirstName.of("Max"),
                        LastName.of("Mustermann")
                ),
                Mail.of("max.mustermann@kicherkrabbe.com")
        );
        update(userLookup);

        // when: the user is removed from the lookup
        remove(userLookup.getUserId());

        // then: the user can not be found by its mail
        var foundUser = findByMail(userLookup.getMail());
        assertThat(foundUser).isNull();
    }

    private void update(UserLookup userLookup) {
        repo.update(userLookup).block();
    }

    private void remove(UserId userId) {
        repo.remove(userId).block();
    }

    private UserLookup findByMail(Mail mail) {
        return repo.findByMail(mail).block();
    }

}
