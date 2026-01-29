package de.bennyboer.kicherkrabbe.users.samples;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.users.*;
import de.bennyboer.kicherkrabbe.users.persistence.lookup.LookupUser;
import lombok.Builder;

@Builder
public class SampleLookupUser {

    @Builder.Default
    private UserId id = UserId.create();

    @Builder.Default
    private Version version = Version.of(1);

    @Builder.Default
    private FullName name = FullName.of(
            FirstName.of("Max"),
            LastName.of("Mustermann")
    );

    @Builder.Default
    private Mail mail = Mail.of("max.mustermann@kicherkrabbe.com");

    public LookupUser toModel() {
        return LookupUser.of(id, version, name, mail);
    }

}
