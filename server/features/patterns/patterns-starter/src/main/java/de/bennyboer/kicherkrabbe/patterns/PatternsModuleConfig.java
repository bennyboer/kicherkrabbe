package de.bennyboer.kicherkrabbe.patterns;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        SecurityConfig.class
})
public class PatternsModuleConfig {

}
