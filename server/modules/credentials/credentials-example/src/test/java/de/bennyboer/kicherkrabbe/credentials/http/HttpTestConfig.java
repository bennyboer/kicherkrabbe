package de.bennyboer.kicherkrabbe.credentials.http;

import de.bennyboer.kicherkrabbe.persistence.MockReactiveTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.ReactiveTransactionManager;

@Configuration
public class HttpTestConfig {

    @Bean
    public ReactiveTransactionManager transactionManager() {
        return new MockReactiveTransactionManager();
    }

}
