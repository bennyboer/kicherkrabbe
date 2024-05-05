package de.bennyboer.kicherkrabbe.users.persistence.lookup;

import de.bennyboer.kicherkrabbe.users.*;
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

    @Test
    void shouldCountUsersInLookup() {
        // when: counting on an empty lookup
        var count = repo.count().block();

        // then: the count is 0
        assertThat(count).isEqualTo(0);

        // when: adding a user in the lookup
        var userLookup1 = UserLookup.of(
                UserId.of("USER_ID_1"),
                FullName.of(
                        FirstName.of("Max"),
                        LastName.of("Mustermann")
                ),
                Mail.of("max.mustermann@kicherkrabbe.com")
        );
        update(userLookup1);

        // then: the count is 1
        count = repo.count().block();
        assertThat(count).isEqualTo(1);

        // when: adding another user in the lookup
        var userLookup2 = UserLookup.of(
                UserId.of("USER_ID_2"),
                FullName.of(
                        FirstName.of("John"),
                        LastName.of("Doe")
                ),
                Mail.of("john.doe@kicherkrabbe.com")
        );
        update(userLookup2);

        // then: the count is 2
        count = repo.count().block();
        assertThat(count).isEqualTo(2);
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
