package de.bennyboer.kicherkrabbe.permissions;

import de.bennyboer.kicherkrabbe.permissions.persistence.PermissionsRepo;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermission;
import de.bennyboer.kicherkrabbe.permissions.persistence.mongo.MongoPermissionsRepo;
import de.bennyboer.kicherkrabbe.persistence.MongoTest;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@MongoTest
public class MongoRepoPermissionsServiceTest extends PermissionsServiceTest {

    private final ReactiveMongoTemplate template;

    @Autowired
    public MongoRepoPermissionsServiceTest(ReactiveMongoTemplate template) {
        this.template = template;
    }

    @Override
    protected PermissionsRepo createRepo() {
        return new MongoPermissionsRepo("test_permissions", template);
    }

    @BeforeEach
    public void setUp() {
        template.remove(MongoPermission.class)
                .inCollection("test_permissions")
                .all()
                .block();
    }

}
