package de.bennyboer.kicherkrabbe.patching;

import de.bennyboer.kicherkrabbe.patching.persistence.mongo.MongoPatchingMetaRepo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

@Configuration
public class PatchingConfig {

    @Bean
    @ConditionalOnMissingBean
    public MongoPatchingMetaRepo mongoPatchingMetaRepo(ReactiveMongoTemplate template) {
        return new MongoPatchingMetaRepo(template);
    }

    @Bean
    @ConditionalOnMissingBean
    public InstanceId patchingInstanceId() {
        return InstanceId.create();
    }

    @Bean
    public PatchingEngine patchingEngine(
            List<DatabasePatch> patches,
            MongoPatchingMetaRepo metaRepo,
            ReactiveMongoTemplate template,
            InstanceId instanceId,
            Optional<Clock> clock
    ) {
        var engine = new PatchingEngine(
                patches,
                metaRepo,
                template,
                instanceId,
                clock.orElse(Clock.systemUTC())
        );

        engine.run().block();

        return engine;
    }

}
