package de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.credentials.adapters.persistence.lookup.CredentialsLookup;
import de.bennyboer.kicherkrabbe.credentials.internal.CredentialsId;
import de.bennyboer.kicherkrabbe.credentials.internal.Name;
import de.bennyboer.kicherkrabbe.credentials.internal.UserId;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import reactor.core.publisher.Mono;

public class MongoCredentialsLookupSerializer implements
        ReadModelSerializer<CredentialsLookup, MongoCredentialsLookup> {

    @Override
    public Mono<MongoCredentialsLookup> serialize(CredentialsLookup readModel) {
        var result = new MongoCredentialsLookup();

        result.id = readModel.getId().getValue();
        result.name = readModel.getName().getValue();
        result.userId = readModel.getUserId().getValue();

        return Mono.just(result);
    }

    @Override
    public Mono<CredentialsLookup> deserialize(MongoCredentialsLookup serialized) {
        return Mono.just(CredentialsLookup.of(
                CredentialsId.of(serialized.id),
                Name.of(serialized.name),
                UserId.of(serialized.userId)
        ));
    }

}
