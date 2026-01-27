package de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo;

public interface ReadModelSerializer<D, S> {

    S serialize(D readModel);

    D deserialize(S serialized);

}
