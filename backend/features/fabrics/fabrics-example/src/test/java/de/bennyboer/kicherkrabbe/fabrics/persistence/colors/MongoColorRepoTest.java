package de.bennyboer.kicherkrabbe.fabrics.persistence.colors;

import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.mongo.MongoColor;
import de.bennyboer.kicherkrabbe.fabrics.persistence.colors.mongo.MongoColorRepo;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoColorRepoTest extends ColorRepoTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoColorRepoTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected ColorRepo createRepo() {
        return new MongoColorRepo("fabrics_colors", template);
    }

    @BeforeEach
    public void clear() {
        template.remove(MongoColor.class)
                .inCollection("fabrics_colors")
                .all()
                .block();
    }

}
