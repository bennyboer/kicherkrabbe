package de.bennyboer.kicherkrabbe.users.persistence.lookup;

import de.bennyboer.kicherkrabbe.commons.UserId;
import de.bennyboer.kicherkrabbe.users.*;
import de.bennyboer.kicherkrabbe.users.samples.SampleLookupUser;
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
        var userLookup = SampleLookupUser.builder().build().toModel();

        // when: the user is updated in the lookup
        update(userLookup);

        // then: the user can be found by its mail
        var foundUser = findByMail(userLookup.getMail());
        assertThat(foundUser).isEqualTo(userLookup);
    }

    @Test
    void shouldRemoveUserFromLookup() {
        // given: a user in the lookup
        var userLookup = SampleLookupUser.builder().build().toModel();
        update(userLookup);

        // when: the user is removed from the lookup
        remove(userLookup.getId());

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
        var userLookup1 = SampleLookupUser.builder()
                .id(UserId.of("USER_ID_1"))
                .mail(Mail.of("max.mustermann@kicherkrabbe.com"))
                .build().toModel();
        update(userLookup1);

        // then: the count is 1
        count = repo.count().block();
        assertThat(count).isEqualTo(1);

        // when: adding another user in the lookup
        var userLookup2 = SampleLookupUser.builder()
                .id(UserId.of("USER_ID_2"))
                .name(FullName.of(
                        FirstName.of("John"),
                        LastName.of("Doe")
                ))
                .mail(Mail.of("john.doe@kicherkrabbe.com"))
                .build().toModel();
        update(userLookup2);

        // then: the count is 2
        count = repo.count().block();
        assertThat(count).isEqualTo(2);
    }

    private void update(LookupUser lookupUser) {
        repo.update(lookupUser).block();
    }

    private void remove(UserId userId) {
        repo.remove(userId).block();
    }

    private LookupUser findByMail(Mail mail) {
        return repo.findByMail(mail).block();
    }

}
