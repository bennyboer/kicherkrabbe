package de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;

public interface VersionedReadModel<ID> {

    ID getId();

    Version getVersion();

}
