package de.bennyboer.kicherkrabbe.products.persistence.lookup.product.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.MongoEventSourcingReadModelRepo;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.links.mongo.MongoLinkType;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.LookupProduct;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.LookupProductPage;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.ProductLookupRepo;
import de.bennyboer.kicherkrabbe.products.product.LinkId;
import de.bennyboer.kicherkrabbe.products.product.LinkType;
import de.bennyboer.kicherkrabbe.products.product.ProductId;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.ReactiveIndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

public class MongoProductLookupRepo
        extends MongoEventSourcingReadModelRepo<ProductId, LookupProduct, MongoLookupProduct>
        implements ProductLookupRepo {

    public MongoProductLookupRepo(ReactiveMongoTemplate template) {
        this("products_product_lookup", template);
    }

    public MongoProductLookupRepo(
            String collectionName,
            ReactiveMongoTemplate template
    ) {
        super(collectionName, template, new MongoLookupProductSerializer());
    }

    @Override
    protected String stringifyId(ProductId productId) {
        return productId.getValue();
    }

    @Override
    public Mono<LookupProduct> findById(ProductId id) {
        Criteria criteria = where("_id").is(id.getValue());
        Query query = query(criteria);

        return template.findOne(query, MongoLookupProduct.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    public Mono<LookupProductPage> findByIds(
            Set<ProductId> ids,
            String searchTerm,
            @Nullable Instant from,
            @Nullable Instant to,
            long skip,
            long limit
    ) {
        Set<String> idValues = ids.stream()
                .map(ProductId::getValue)
                .collect(Collectors.toSet());

        Criteria criteria = where("_id").in(idValues);

        if (from != null && to != null) {
            criteria = criteria.and("createdAt").gte(from).lt(to);
        } else if (from != null) {
            criteria = criteria.and("createdAt").gte(from);
        } else if (to != null) {
            criteria = criteria.and("createdAt").lt(to);
        }

        if (!searchTerm.isBlank()) {
            String quotedSearchTerm = Pattern.quote(searchTerm);

            Criteria searchTermCriteria = where("number").regex(quotedSearchTerm, "i");
            criteria = criteria.andOperator(searchTermCriteria);
        }

        Sort sort = Sort.by(DESC, "createdAt");
        Query query = Query.query(criteria)
                .with(sort)
                .skip((int) skip)
                .limit((int) limit);

        return template.count(Query.query(criteria), MongoLookupProduct.class, collectionName)
                .flatMap(total -> template.find(query, MongoLookupProduct.class, collectionName)
                        .map(serializer::deserialize)
                        .collectList()
                        .map(products -> LookupProductPage.of(total, products)));
    }

    @Override
    public Flux<LookupProduct> findByLink(LinkType linkType, LinkId linkId) {
        MongoLinkType mongoLinkType = switch (linkType) {
            case PATTERN -> MongoLinkType.PATTERN;
            case FABRIC -> MongoLinkType.FABRIC;
        };

        Criteria criteria = where("links.id").is(linkId.getValue())
                .and("links.type").is(mongoLinkType);
        Query query = query(criteria);

        return template.find(query, MongoLookupProduct.class, collectionName)
                .map(serializer::deserialize);
    }

    @Override
    protected Mono<Void> initializeIndices(ReactiveIndexOperations indexOps) {
        var idCreatedAtIndex = new Index().on("_id", ASC)
                .on("createdAt", DESC);
        var linkIdIndex = new Index().on("links.id", ASC);

        return indexOps.createIndex(idCreatedAtIndex)
                .then(indexOps.createIndex(linkIdIndex))
                .then();
    }

}
