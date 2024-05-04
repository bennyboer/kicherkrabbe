package de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup;

import de.bennyboer.kicherkrabbe.credentials.CredentialsId;
import de.bennyboer.kicherkrabbe.credentials.Name;
import de.bennyboer.kicherkrabbe.credentials.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class CredentialsLookupRepoTest {

    private CredentialsLookupRepo repo;

    protected abstract CredentialsLookupRepo createRepo();

    @BeforeEach
    public void setUp() {
        repo = createRepo();
    }

    @Test
    void shouldBeAbleToFindCredentialsByName() {
        // given: a lookup to save
        var lookup = CredentialsLookup.of(
                CredentialsId.create(),
                Name.of("Some name"),
                UserId.of("USER_ID_1")
        );

        // when: saving the lookup
        update(lookup);

        // then: the credentials can be found by their name
        var credentials = findCredentialsByName(lookup.getName());
        assertThat(credentials).isEqualTo(lookup);

        // when: saving another lookup with another ID
        var anotherLookup = CredentialsLookup.of(
                CredentialsId.create(),
                Name.of("Some other name"),
                UserId.of("USER_ID_2")
        );
        update(anotherLookup);

        // then: the first lookup can still be found by their name
        var foundCredentials = findCredentialsByName(lookup.getName());
        assertThat(foundCredentials).isEqualTo(lookup);

        // and: the second lookup can be found by their name
        var foundAnotherCredentials = findCredentialsByName(anotherLookup.getName());
        assertThat(foundAnotherCredentials).isEqualTo(anotherLookup);

        // when: updating the first lookup since the name changed
        var updatedLookup = CredentialsLookup.of(
                lookup.getId(),
                Name.of("Some new name"),
                UserId.of("USER_ID_1")
        );
        update(updatedLookup);

        // then: the first lookup can no longer be found by their old name
        var notFoundCredentials = findCredentialsByName(lookup.getName());
        assertThat(notFoundCredentials).isNull();

        // and: the first lookup can be found by their new name
        var foundUpdatedCredentials = findCredentialsByName(updatedLookup.getName());
        assertThat(foundUpdatedCredentials).isEqualTo(updatedLookup);
    }

    @Test
    void shouldRemoveCredentials() {
        // given: a lookup to save
        var lookup = CredentialsLookup.of(
                CredentialsId.create(),
                Name.of("Some name"),
                UserId.of("USER_ID")
        );

        // when: saving the lookup
        update(lookup);

        // then: the credentials can be found by their name
        var credentials = findCredentialsByName(lookup.getName());
        assertThat(credentials).isEqualTo(lookup);

        // when: removing the lookup
        remove(lookup.getId());

        // then: the credentials can no longer be found by their name
        var notFoundCredentials = findCredentialsByName(lookup.getName());
        assertThat(notFoundCredentials).isNull();
    }

    @Test
    void shouldFindCredentialsByUserId() {
        // given: a lookup
        var lookup = CredentialsLookup.of(
                CredentialsId.create(),
                Name.of("Some name"),
                UserId.of("USER_ID")
        );
        update(lookup);

        // then: the credentials can be found by their user ID
        var credentials = findCredentialsByUserId(lookup.getUserId());
        assertThat(credentials.size()).isEqualTo(1);
        assertThat(credentials.getFirst().getId()).isEqualTo(lookup.getId());

        // when: saving another lookup with the same user ID
        var anotherLookup = CredentialsLookup.of(
                CredentialsId.create(),
                Name.of("Some other name"),
                UserId.of("USER_ID")
        );
        update(anotherLookup);

        // then: there are two credentials with the same user ID
        credentials = findCredentialsByUserId(lookup.getUserId());
        assertThat(credentials.size()).isEqualTo(2);
        assertThat(credentials.stream().map(CredentialsLookup::getId)).contains(lookup.getId(), anotherLookup.getId());

        // when: saving another lookup with another user ID
        var yetAnotherLookup = CredentialsLookup.of(
                CredentialsId.create(),
                Name.of("Some other name"),
                UserId.of("ANOTHER_USER_ID")
        );
        update(yetAnotherLookup);

        // then: there are still two credentials with the same user ID
        credentials = findCredentialsByUserId(lookup.getUserId());
        assertThat(credentials.size()).isEqualTo(2);

        // and: there is one credential with the other user ID
        credentials = findCredentialsByUserId(UserId.of("ANOTHER_USER_ID"));
        assertThat(credentials.size()).isEqualTo(1);
        assertThat(credentials.getFirst().getId()).isEqualTo(yetAnotherLookup.getId());

        // when: removing the credentials with the first user ID
        remove(lookup.getId());
        remove(anotherLookup.getId());

        // then: there is no credential with the first user ID
        credentials = findCredentialsByUserId(lookup.getUserId());
        assertThat(credentials.size()).isEqualTo(0);
    }

    private void update(CredentialsLookup lookup) {
        repo.update(lookup).block();
    }

    private CredentialsLookup findCredentialsByName(Name name) {
        return repo.findCredentialsByName(name).block();
    }

    private List<CredentialsLookup> findCredentialsByUserId(UserId userId) {
        return repo.findCredentialsByUserId(userId).collectList().block();
    }

    private void remove(CredentialsId id) {
        repo.remove(id).block();
    }

}
