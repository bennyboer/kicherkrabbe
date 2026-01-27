package de.bennyboer.kicherkrabbe.products;

import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import de.bennyboer.kicherkrabbe.products.counter.CounterService;
import de.bennyboer.kicherkrabbe.products.http.ProductsHttpConfig;
import de.bennyboer.kicherkrabbe.products.messaging.ProductsMessaging;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.links.LinkLookupRepo;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.links.mongo.MongoLinkLookupRepo;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.ProductLookupRepo;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.mongo.MongoProductLookupRepo;
import de.bennyboer.kicherkrabbe.products.product.ProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.transaction.ReactiveTransactionManager;

@Import({
        ProductsAggregateConfig.class,
        ProductsPermissionsConfig.class,
        ProductsHttpConfig.class,
        ProductsMessaging.class,
        SecurityConfig.class
})
@Configuration
public class ProductsModuleConfig {

    @Bean
    public ProductLookupRepo productLookupRepo(ReactiveMongoTemplate template) {
        return new MongoProductLookupRepo(template);
    }

    @Bean
    public LinkLookupRepo linkRepo(ReactiveMongoTemplate template) {
        return new MongoLinkLookupRepo(template);
    }

    @Bean
    public ProductsModule productsModule(
            @Qualifier("productsProductService") ProductService productService,
            @Qualifier("productsCounterService") CounterService counterService,
            ProductLookupRepo productLookupRepo,
            LinkLookupRepo linkLookupRepo,
            @Qualifier("productsPermissionsService") PermissionsService permissionsService,
            ReactiveTransactionManager transactionManager
    ) {
        return new ProductsModule(
                productService,
                counterService,
                productLookupRepo,
                linkLookupRepo,
                permissionsService,
                transactionManager
        );
    }

}
