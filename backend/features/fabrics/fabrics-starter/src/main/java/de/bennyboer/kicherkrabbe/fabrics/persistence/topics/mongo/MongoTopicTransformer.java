package de.bennyboer.kicherkrabbe.fabrics.persistence.topics.mongo;

import de.bennyboer.kicherkrabbe.fabrics.TopicId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.Topic;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.TopicName;

public class MongoTopicTransformer {

    public static MongoTopic toMongo(Topic topic) {
        var result = new MongoTopic();

        result.id = topic.getId().getValue();
        result.name = topic.getName().getValue();

        return result;
    }

    public static Topic fromMongo(MongoTopic topic) {
        TopicId id = TopicId.of(topic.id);
        TopicName name = TopicName.of(topic.name);

        return Topic.of(id, name);
    }

}
