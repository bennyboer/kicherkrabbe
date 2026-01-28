package de.bennyboer.kicherkrabbe.fabrics.persistence.colors.mongo;

import de.bennyboer.kicherkrabbe.fabrics.ColorId;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.Color;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.ColorRepo;
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

public class MongoColorRepo implements ColorRepo {

    private final String collectionName;

    private final ReactiveMongoTemplate template;

    public MongoColorRepo(ReactiveMongoTemplate template) {
        this("fabrics_colors", template);
    }

    public MongoColorRepo(String collectionName, ReactiveMongoTemplate template) {
        this.collectionName = collectionName;
        this.template = template;
    }

    @Override
    public Mono<Color> save(Color topic) {
        return template.save(MongoColorTransformer.toMongo(topic), collectionName)
                .map(MongoColorTransformer::fromMongo);
    }

    @Override
    public Mono<Void> removeById(ColorId id) {
        Criteria criteria = where("_id").is(id.getValue());
        Query query = query(criteria);

        return template.remove(query, collectionName).then();
    }

    @Override
    public Flux<Color> findByIds(Collection<ColorId> ids) {
        Set<String> topicIds = ids.stream()
                .map(ColorId::getValue)
                .collect(Collectors.toSet());

        Criteria criteria = where("_id").in(topicIds);
        Query query = query(criteria);

        return template.find(query, MongoColor.class, collectionName)
                .map(MongoColorTransformer::fromMongo);
    }

    @Override
    public Flux<Color> findAll() {
        return template.findAll(MongoColor.class, collectionName)
                .map(MongoColorTransformer::fromMongo);
    }

}
