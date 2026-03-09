package de.bennyboer.kicherkrabbe.fabrics;

import de.bennyboer.kicherkrabbe.fabrics.persistence.patches.SetKindInLookupPatch;
import de.bennyboer.kicherkrabbe.patching.DatabasePatch;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FabricsPatchingConfig {

    @Bean
    public DatabasePatch setKindInLookupPatch() {
        return new SetKindInLookupPatch();
    }

}
