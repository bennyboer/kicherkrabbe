package de.bennyboer.kicherkrabbe.topics.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.topics.TopicId;
import de.bennyboer.kicherkrabbe.topics.TopicName;
import de.bennyboer.kicherkrabbe.topics.persistence.lookup.LookupTopic;

public class MongoLookupTopicSerializer implements ReadModelSerializer<LookupTopic, MongoLookupTopic> {

    @Override
    public MongoLookupTopic serialize(LookupTopic readModel) {
        var result = new MongoLookupTopic();

        result.id = readModel.getId().getValue();
        result.version = readModel.getVersion().getValue();
        result.name = readModel.getName().getValue();
        result.createdAt = readModel.getCreatedAt();

        return result;
    }

    @Override
    public LookupTopic deserialize(MongoLookupTopic serialized) {
        var id = TopicId.of(serialized.id);
        var version = Version.of(serialized.version);
        var name = TopicName.of(serialized.name);
        var createdAt = serialized.createdAt;

        return LookupTopic.of(
                id,
                version,
                name,
                createdAt
        );
    }

}
