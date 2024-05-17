package de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes;

import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.mongo.MongoFabricType;
import de.bennyboer.kicherkrabbe.fabrics.persistence.fabrictypes.mongo.MongoFabricTypeRepo;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoFabricTypeRepoTest extends FabricTypeRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoFabricTypeRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected FabricTypeRepo createRepo() {
        return new MongoFabricTypeRepo("fabrics_fabric_types", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoFabricType.class)
                .inCollection("fabrics_fabric_types")
                .all()
                .block();
    }

}
