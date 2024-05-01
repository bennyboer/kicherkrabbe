package de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.users.*;
import de.bennyboer.kicherkrabbe.users.adapters.persistence.lookup.UserLookup;
import reactor.core.publisher.Mono;

public class MongoUserLookupSerializer implements
        ReadModelSerializer<UserLookup, MongoUserLookup> {

    @Override
    public Mono<MongoUserLookup> serialize(UserLookup readModel) {
        var result = new MongoUserLookup();

        result.userId = readModel.getUserId().getValue();
        result.firstName = readModel.getName().getFirstName().getValue();
        result.lastName = readModel.getName().getLastName().getValue();
        result.mail = readModel.getMail().getValue();

        return Mono.just(result);
    }

    @Override
    public Mono<UserLookup> deserialize(MongoUserLookup serialized) {
        return Mono.just(UserLookup.of(
                UserId.of(serialized.userId),
                FullName.of(
                        FirstName.of(serialized.firstName),
                        LastName.of(serialized.lastName)
                ),
                Mail.of(serialized.mail)
        ));
    }

}
