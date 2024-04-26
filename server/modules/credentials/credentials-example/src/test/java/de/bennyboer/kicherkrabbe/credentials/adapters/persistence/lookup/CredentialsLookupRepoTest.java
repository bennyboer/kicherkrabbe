package de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup;

import de.bennyboer.kicherkrabbe.credentials.internal.CredentialsId;
import de.bennyboer.kicherkrabbe.credentials.internal.Name;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        var lookup = CredentialsLookup.of(CredentialsId.create(), Name.of("Some name"));

        // when: saving the lookup
        update(lookup);

        // then: the credentials can be found by their name
        var credentialsId = findCredentialsIdByName(lookup.getName());
        assertThat(credentialsId).isEqualTo(lookup.getId());

        // when: saving another lookup with another ID
        var anotherLookup = CredentialsLookup.of(CredentialsId.create(), Name.of("Some other name"));
        update(anotherLookup);

        // then: the first lookup can still be found by their name
        var foundCredentialsId = findCredentialsIdByName(lookup.getName());

        // and: the second lookup can be found by their name
        var foundAnotherCredentialsId = findCredentialsIdByName(anotherLookup.getName());
        assertThat(foundAnotherCredentialsId).isEqualTo(anotherLookup.getId());

        // when: updating the first lookup since the name changed
        var updatedLookup = CredentialsLookup.of(lookup.getId(), Name.of("Some new name"));
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
        var lookup = CredentialsLookup.of(CredentialsId.create(), Name.of("Some name"));

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

    private void update(CredentialsLookup lookup) {
        repo.update(lookup).block();
    }

    private CredentialsId findCredentialsIdByName(Name name) {
        return repo.findCredentialsIdByName(name).block();
    }

    private void remove(CredentialsId id) {
        repo.remove(id).block();
    }

}
