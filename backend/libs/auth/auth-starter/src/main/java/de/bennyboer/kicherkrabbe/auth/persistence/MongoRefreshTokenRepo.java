package de.bennyboer.kicherkrabbe.auth.persistence;

import de.bennyboer.kicherkrabbe.auth.tokens.RefreshToken;
import de.bennyboer.kicherkrabbe.auth.tokens.RefreshTokenId;
import de.bennyboer.kicherkrabbe.auth.tokens.RefreshTokenRepo;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.data.domain.Sort.Direction.ASC;

@AllArgsConstructor
public class MongoRefreshTokenRepo implements RefreshTokenRepo {

    private static final String COLLECTION_NAME = "refresh_tokens";

    private final ReactiveMongoTemplate template;

    public Mono<Void> initialize() {
        var indexOps = template.indexOps(COLLECTION_NAME);

        return indexOps.createIndex(new Index().on("tokenValue", ASC).unique())
                .then(indexOps.createIndex(new Index().on("family", ASC)))
                .then(indexOps.createIndex(new Index().on("userId", ASC)))
                .then(indexOps.createIndex(new Index().on("expiresAt", ASC).expire(Duration.ZERO)))
                .then();
    }

    @Override
    public Mono<Void> save(RefreshToken token) {
        return template.save(toMongo(token), COLLECTION_NAME).then();
    }

    @Override
    public Mono<RefreshToken> findByTokenValue(String tokenValue) {
        var query = Query.query(Criteria.where("tokenValue").is(tokenValue));
        return template.findOne(query, MongoRefreshToken.class, COLLECTION_NAME)
                .map(this::fromMongo);
    }

    @Override
    public Mono<Boolean> markAsUsedIfNotAlready(String tokenValue) {
        var query = Query.query(Criteria.where("tokenValue").is(tokenValue).and("used").is(false));
        var update = new Update().set("used", true);
        var options = FindAndModifyOptions.options().returnNew(false);

        return template.findAndModify(query, update, options, MongoRefreshToken.class, COLLECTION_NAME)
                .map(doc -> true)
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Void> revokeFamily(String family) {
        var query = Query.query(Criteria.where("family").is(family));
        return template.remove(query, COLLECTION_NAME).then();
    }

    @Override
    public Mono<Void> revokeByUserId(String userId) {
        var query = Query.query(Criteria.where("userId").is(userId));
        return template.remove(query, COLLECTION_NAME).then();
    }

    private MongoRefreshToken toMongo(RefreshToken token) {
        var doc = new MongoRefreshToken();
        doc.id = token.getId().getValue();
        doc.tokenValue = token.getTokenValue();
        doc.userId = token.getUserId();
        doc.family = token.getFamily();
        doc.used = token.isUsed();
        doc.expiresAt = token.getExpiresAt();
        doc.createdAt = token.getCreatedAt();
        return doc;
    }

    private RefreshToken fromMongo(MongoRefreshToken doc) {
        return RefreshToken.of(
                RefreshTokenId.of(doc.id),
                doc.tokenValue,
                doc.userId,
                doc.family,
                doc.used,
                doc.expiresAt,
                doc.createdAt
        );
    }

}
