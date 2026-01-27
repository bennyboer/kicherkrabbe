package de.bennyboer.kicherkrabbe.frontend;

import de.bennyboer.kicherkrabbe.frontend.http.FrontendHttpConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({FrontendHttpConfig.class})
public class FrontendModuleConfig {

}
