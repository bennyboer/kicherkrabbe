package de.bennyboer.kicherkrabbe.assets.persistence.lookup;

import de.bennyboer.kicherkrabbe.assets.persistence.lookup.mongo.MongoAssetLookupRepo;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.mongo.MongoLookupAsset;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoAssetLookupRepoTest extends AssetLookupRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoAssetLookupRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected AssetLookupRepo createRepo() {
        return new MongoAssetLookupRepo("assets_lookup_test", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoLookupAsset.class)
                .inCollection("assets_lookup_test")
                .all()
                .block();
    }

}
