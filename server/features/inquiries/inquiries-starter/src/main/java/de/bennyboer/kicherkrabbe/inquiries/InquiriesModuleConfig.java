package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.inquiries.http.InquiriesHttpConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        InquiriesHttpConfig.class,
        SecurityConfig.class
})
public class InquiriesModuleConfig {

    @Bean
    public InquiriesModule inquiriesModule() {
        return new InquiriesModule();
    }

}
