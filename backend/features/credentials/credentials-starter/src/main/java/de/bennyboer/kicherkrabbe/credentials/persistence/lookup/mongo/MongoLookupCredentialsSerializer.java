package de.bennyboer.kicherkrabbe.credentials.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.credentials.CredentialsId;
import de.bennyboer.kicherkrabbe.credentials.Name;
import de.bennyboer.kicherkrabbe.commons.UserId;
import de.bennyboer.kicherkrabbe.credentials.persistence.lookup.LookupCredentials;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;

public class MongoLookupCredentialsSerializer
        implements ReadModelSerializer<LookupCredentials, MongoLookupCredentials> {

    @Override
    public MongoLookupCredentials serialize(LookupCredentials readModel) {
        var result = new MongoLookupCredentials();

        result.id = readModel.getId().getValue();
        result.version = readModel.getVersion().getValue();
        result.name = readModel.getName().getValue();
        result.userId = readModel.getUserId().getValue();

        return result;
    }

    @Override
    public LookupCredentials deserialize(MongoLookupCredentials serialized) {
        return LookupCredentials.of(
                CredentialsId.of(serialized.id),
                Version.of(serialized.version),
                Name.of(serialized.name),
                UserId.of(serialized.userId)
        );
    }

}
