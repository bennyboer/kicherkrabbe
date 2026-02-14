package de.bennyboer.kicherkrabbe.users.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.commons.UserId;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.users.*;
import de.bennyboer.kicherkrabbe.users.persistence.lookup.LookupUser;

public class MongoLookupUserSerializer implements ReadModelSerializer<LookupUser, MongoLookupUser> {

    @Override
    public MongoLookupUser serialize(LookupUser readModel) {
        var result = new MongoLookupUser();

        result.id = readModel.getId().getValue();
        result.version = readModel.getVersion().getValue();
        result.firstName = readModel.getName().getFirstName().getValue();
        result.lastName = readModel.getName().getLastName().getValue();
        result.mail = readModel.getMail().getValue();

        return result;
    }

    @Override
    public LookupUser deserialize(MongoLookupUser serialized) {
        return LookupUser.of(
                UserId.of(serialized.id),
                Version.of(serialized.version),
                FullName.of(
                        FirstName.of(serialized.firstName),
                        LastName.of(serialized.lastName)
                ),
                Mail.of(serialized.mail)
        );
    }

}
