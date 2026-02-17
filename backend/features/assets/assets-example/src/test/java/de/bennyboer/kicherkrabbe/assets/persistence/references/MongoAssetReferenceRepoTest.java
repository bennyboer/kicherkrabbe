package de.bennyboer.kicherkrabbe.assets.persistence.references;

import de.bennyboer.kicherkrabbe.assets.persistence.references.mongo.MongoAssetReference;
import de.bennyboer.kicherkrabbe.assets.persistence.references.mongo.MongoAssetReferenceRepo;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoAssetReferenceRepoTest extends AssetReferenceRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoAssetReferenceRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected AssetReferenceRepo createRepo() {
        return new MongoAssetReferenceRepo("assets_references_test", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoAssetReference.class)
                .inCollection("assets_references_test")
                .all()
                .block();
    }

}
