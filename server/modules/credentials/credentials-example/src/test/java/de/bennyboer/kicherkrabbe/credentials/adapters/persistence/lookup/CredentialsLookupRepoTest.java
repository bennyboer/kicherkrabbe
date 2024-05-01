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
        var credentialsId = findCredentialsIdByName(lookup.getName());
        assertThat(credentialsId).isEqualTo(lookup.getId());

        // when: saving another lookup with another ID
        var anotherLookup = CredentialsLookup.of(
                CredentialsId.create(),
                Name.of("Some other name"),
                UserId.of("USER_ID_2")
        );
        update(anotherLookup);

        // then: the first lookup can still be found by their name
        var foundCredentialsId = findCredentialsIdByName(lookup.getName());

        // and: the second lookup can be found by their name
        var foundAnotherCredentialsId = findCredentialsIdByName(anotherLookup.getName());
        assertThat(foundAnotherCredentialsId).isEqualTo(anotherLookup.getId());

        // when: updating the first lookup since the name changed
        var updatedLookup = CredentialsLookup.of(
                lookup.getId(),
                Name.of("Some new name"),
                UserId.of("USER_ID_1")
        );
        update(updatedLookup);

        // then: the first lookup can no longer be found by their old name
        var notFoundCredentialsId = findCredentialsIdByName(lookup.getName());
        assertThat(notFoundCredentialsId).isNull();

        // and: the first lookup can be found by their new name
        var foundUpdatedCredentialsId = findCredentialsIdByName(updatedLookup.getName());
        assertThat(foundUpdatedCredentialsId).isEqualTo(lookup.getId());
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
        var credentialsId = findCredentialsIdByName(lookup.getName());
        assertThat(credentialsId).isEqualTo(lookup.getId());

        // when: removing the lookup
        remove(lookup.getId());

        // then: the credentials can no longer be found by their name
        var notFoundCredentialsId = findCredentialsIdByName(lookup.getName());
        assertThat(notFoundCredentialsId).isNull();
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
        var credentialsIds = findCredentialsIdByUserId(lookup.getUserId());
        assertThat(credentialsIds.size()).isEqualTo(1);
        assertThat(credentialsIds.get(0)).isEqualTo(lookup.getId());

        // when: saving another lookup with the same user ID
        var anotherLookup = CredentialsLookup.of(
                CredentialsId.create(),
                Name.of("Some other name"),
                UserId.of("USER_ID")
        );
        update(anotherLookup);

        // then: there are two credentials with the same user ID
        credentialsIds = findCredentialsIdByUserId(lookup.getUserId());
        assertThat(credentialsIds.size()).isEqualTo(2);
        assertThat(credentialsIds).contains(lookup.getId(), anotherLookup.getId());

        // when: saving another lookup with another user ID
        var yetAnotherLookup = CredentialsLookup.of(
                CredentialsId.create(),
                Name.of("Some other name"),
                UserId.of("ANOTHER_USER_ID")
        );
        update(yetAnotherLookup);

        // then: there are still two credentials with the same user ID
        credentialsIds = findCredentialsIdByUserId(lookup.getUserId());
        assertThat(credentialsIds.size()).isEqualTo(2);

        // and: there is one credential with the other user ID
        credentialsIds = findCredentialsIdByUserId(UserId.of("ANOTHER_USER_ID"));
        assertThat(credentialsIds.size()).isEqualTo(1);
        assertThat(credentialsIds.get(0)).isEqualTo(yetAnotherLookup.getId());

        // when: removing the credentials with the first user ID
        remove(lookup.getId());
        remove(anotherLookup.getId());

        // then: there is no credential with the first user ID
        credentialsIds = findCredentialsIdByUserId(lookup.getUserId());
        assertThat(credentialsIds.size()).isEqualTo(0);
    }

    private void update(CredentialsLookup lookup) {
        repo.update(lookup).block();
    }

    private CredentialsId findCredentialsIdByName(Name name) {
        return repo.findCredentialsIdByName(name).block();
    }

    private List<CredentialsId> findCredentialsIdByUserId(UserId userId) {
        return repo.findCredentialsIdByUserId(userId).collectList().block();
    }

    private void remove(CredentialsId id) {
        repo.remove(id).block();
    }

}
