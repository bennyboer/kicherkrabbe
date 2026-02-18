package de.bennyboer.kicherkrabbe.assets;

import de.bennyboer.kicherkrabbe.assets.http.AssetsHttpConfig;
import de.bennyboer.kicherkrabbe.assets.image.ImageVariantService;
import de.bennyboer.kicherkrabbe.assets.messaging.AssetsMessaging;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.AssetLookupRepo;
import de.bennyboer.kicherkrabbe.assets.persistence.lookup.mongo.MongoAssetLookupRepo;
import de.bennyboer.kicherkrabbe.assets.persistence.references.AssetReferenceRepo;
import de.bennyboer.kicherkrabbe.assets.persistence.references.mongo.MongoAssetReferenceRepo;
import de.bennyboer.kicherkrabbe.assets.storage.DelegatingStorageService;
import de.bennyboer.kicherkrabbe.assets.storage.StorageService;
import de.bennyboer.kicherkrabbe.assets.storage.file.FileStorageService;
import de.bennyboer.kicherkrabbe.auth.SecurityConfig;
import de.bennyboer.kicherkrabbe.permissions.PermissionsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.nio.file.Path;

@Configuration
@Import({
        AssetsAggregateConfig.class,
        AssetsPermissionsConfig.class,
        AssetsHttpConfig.class,
        AssetsMessaging.class,
        SecurityConfig.class
})
public class AssetsModuleConfig {

    @Bean
    public StorageService storageService(@Value("${assets.storage.file.root-location:.kicherkrabbe-assets}") String rootLocation) {
        var rootPath = Path.of(rootLocation);
        var fileStorageService = new FileStorageService(rootPath);

        return new DelegatingStorageService(fileStorageService);
    }

    @Bean
    public ImageVariantService imageVariantService(StorageService storageService) {
        return new ImageVariantService(storageService);
    }

    @Bean
    public AssetReferenceRepo assetReferenceRepo(ReactiveMongoTemplate template) {
        return new MongoAssetReferenceRepo(template);
    }

    @Bean
    public AssetLookupRepo assetLookupRepo(ReactiveMongoTemplate template) {
        return new MongoAssetLookupRepo(template);
    }

    @Bean
    public AssetsModule assetsModule(
            AssetService assetService,
            @Qualifier("assetsPermissionsService") PermissionsService permissionsService,
            StorageService storageService,
            ImageVariantService imageVariantService,
            AssetReferenceRepo assetReferenceRepo,
            AssetLookupRepo assetLookupRepo
    ) {
        return new AssetsModule(
                assetService,
                permissionsService,
                storageService,
                imageVariantService,
                assetReferenceRepo,
                assetLookupRepo
        );
    }

}
