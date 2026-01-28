package de.bennyboer.kicherkrabbe.mailing.external;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailApiConfig {

    @Bean
    public MailApi mailApi() {
        return new MailHttpApi();
    }

}
