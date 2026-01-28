package de.bennyboer.kicherkrabbe.patterns.persistence.categories.mongo;

import de.bennyboer.kicherkrabbe.patterns.PatternCategory;
import de.bennyboer.kicherkrabbe.patterns.PatternCategoryId;
import de.bennyboer.kicherkrabbe.patterns.persistence.categories.PatternCategoryRepo;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoPatternCategoryRepo implements PatternCategoryRepo {

    private final String collectionName;

    private final ReactiveMongoTemplate template;

    public MongoPatternCategoryRepo(ReactiveMongoTemplate template) {
        this("patterns_categories", template);
    }

    public MongoPatternCategoryRepo(String collectionName, ReactiveMongoTemplate template) {
        this.collectionName = collectionName;
        this.template = template;
    }

    @Override
    public Mono<PatternCategory> save(PatternCategory category) {
        return template.save(MongoPatternCategoryTransformer.toMongo(category), collectionName)
                .map(MongoPatternCategoryTransformer::fromMongo);
    }

    @Override
    public Mono<Void> removeById(PatternCategoryId id) {
        Criteria criteria = where("_id").is(id.getValue());
        Query query = query(criteria);

        return template.remove(query, collectionName).then();
    }

    @Override
    public Flux<PatternCategory> findByIds(Set<PatternCategoryId> ids) {
        Set<String> categoryIds = ids.stream()
                .map(PatternCategoryId::getValue)
                .collect(Collectors.toSet());

        Criteria criteria = where("_id").in(categoryIds);
        Query query = query(criteria);

        return template.find(query, MongoPatternCategory.class, collectionName)
                .map(MongoPatternCategoryTransformer::fromMongo);
    }

    @Override
    public Flux<PatternCategory> findAll() {
        return template.findAll(MongoPatternCategory.class, collectionName)
                .map(MongoPatternCategoryTransformer::fromMongo);
    }

}
