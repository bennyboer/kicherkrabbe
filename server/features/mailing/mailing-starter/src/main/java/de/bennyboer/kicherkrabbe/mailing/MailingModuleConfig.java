package de.bennyboer.kicherkrabbe.mailing;

import de.bennyboer.kicherkrabbe.mailing.http.MailingHttpConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({
        MailingHttpConfig.class
})
@Configuration
public class MailingModuleConfig {

    @Bean
    public MailingModule mailingModule() {
        return new MailingModule();
    }

}
