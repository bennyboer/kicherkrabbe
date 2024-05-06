package de.bennyboer.kicherkrabbe.users.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.users.*;
import de.bennyboer.kicherkrabbe.users.persistence.lookup.LookupUser;

public class MongoLookupUserSerializer implements ReadModelSerializer<LookupUser, MongoLookupUser> {

    @Override
    public MongoLookupUser serialize(LookupUser readModel) {
        var result = new MongoLookupUser();

        result.userId = readModel.getUserId().getValue();
        result.firstName = readModel.getName().getFirstName().getValue();
        result.lastName = readModel.getName().getLastName().getValue();
        result.mail = readModel.getMail().getValue();

        return result;
    }

    @Override
    public LookupUser deserialize(MongoLookupUser serialized) {
        return LookupUser.of(
                UserId.of(serialized.userId),
                FullName.of(
                        FirstName.of(serialized.firstName),
                        LastName.of(serialized.lastName)
                ),
                Mail.of(serialized.mail)
        );
    }

}
