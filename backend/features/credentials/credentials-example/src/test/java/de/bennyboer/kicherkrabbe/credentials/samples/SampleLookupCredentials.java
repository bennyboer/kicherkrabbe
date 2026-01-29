package de.bennyboer.kicherkrabbe.credentials.samples;

import de.bennyboer.kicherkrabbe.credentials.CredentialsId;
import de.bennyboer.kicherkrabbe.credentials.Name;
import de.bennyboer.kicherkrabbe.credentials.UserId;
import de.bennyboer.kicherkrabbe.credentials.persistence.lookup.LookupCredentials;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import lombok.Builder;

@Builder
public class SampleLookupCredentials {

    @Builder.Default
    private CredentialsId id = CredentialsId.create();

    @Builder.Default
    private Version version = Version.of(1);

    @Builder.Default
    private Name name = Name.of("john.doe");

    @Builder.Default
    private UserId userId = UserId.of("USER_ID");

    public LookupCredentials toModel() {
        return LookupCredentials.of(id, version, name, userId);
    }

}
