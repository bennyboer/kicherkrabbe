package de.bennyboer.kicherkrabbe.telegram.external;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramApiConfig {

    @Bean
    public TelegramApi telegramApi() {
        return new TelegramHttpApi();
    }

}
