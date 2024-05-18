package de.bennyboer.kicherkrabbe.assets;

import de.bennyboer.kicherkrabbe.assets.http.AssetsHttpConfig;
import de.bennyboer.kicherkrabbe.assets.messaging.AssetsMessaging;
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
    public AssetsModule assetsModule(
            AssetService assetService,
            @Qualifier("assetsPermissionsService") PermissionsService permissionsService,
            StorageService storageService
    ) {
        return new AssetsModule(assetService, permissionsService, storageService);
    }

}
