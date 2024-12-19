package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.inquiries.http.InquiriesHttpConfig;
import de.bennyboer.kicherkrabbe.inquiries.settings.SettingsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        InquiriesAggregateConfig.class,
        InquiriesHttpConfig.class,
        SecurityConfig.class
})
public class InquiriesModuleConfig {

    @Bean
    public InquiriesModule inquiriesModule(InquiryService inquiryService, SettingsService settingsService) {
        return new InquiriesModule(inquiryService, settingsService);
    }

}
