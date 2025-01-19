package de.bennyboer.kicherkrabbe.products;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductsModuleConfig {

    @Bean
    public ProductsModule productsModule() {
        return new ProductsModule();
    }

}
