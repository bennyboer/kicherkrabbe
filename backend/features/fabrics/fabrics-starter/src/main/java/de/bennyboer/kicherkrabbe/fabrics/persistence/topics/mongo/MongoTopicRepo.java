package de.bennyboer.kicherkrabbe.fabrics.persistence.topics.mongo;

import de.bennyboer.kicherkrabbe.fabrics.TopicId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.Topic;
import de.bennyboer.kicherkrabbe.fabrics.persistence.topics.TopicRepo;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoTopicRepo implements TopicRepo {

    private final String collectionName;

    private final ReactiveMongoTemplate template;

    public MongoTopicRepo(ReactiveMongoTemplate template) {
        this("fabrics_topics", template);
    }

    public MongoTopicRepo(String collectionName, ReactiveMongoTemplate template) {
        this.collectionName = collectionName;
        this.template = template;
    }

    @Override
    public Mono<Topic> save(Topic topic) {
        return template.save(MongoTopicTransformer.toMongo(topic), collectionName)
                .map(MongoTopicTransformer::fromMongo);
    }

    @Override
    public Mono<Void> removeById(TopicId id) {
        Criteria criteria = where("_id").is(id.getValue());
        Query query = query(criteria);

        return template.remove(query, collectionName).then();
    }

    @Override
    public Flux<Topic> findByIds(Collection<TopicId> ids) {
        Set<String> topicIds = ids.stream()
                .map(TopicId::getValue)
                .collect(Collectors.toSet());

        Criteria criteria = where("_id").in(topicIds);
        Query query = query(criteria);

        return template.find(query, MongoTopic.class, collectionName)
                .map(MongoTopicTransformer::fromMongo);
    }

    @Override
    public Flux<Topic> findAll() {
        return template.findAll(MongoTopic.class, collectionName)
                .map(MongoTopicTransformer::fromMongo);
    }

}
