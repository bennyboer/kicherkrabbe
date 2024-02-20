package de.bennyboer.kicherkrabbe.app.plugins.frontend;

import io.javalin.config.JavalinConfig;
import io.javalin.plugin.Plugin;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import static io.javalin.http.staticfiles.Location.CLASSPATH;

@AllArgsConstructor
public class FrontendPlugin extends Plugin<Void> {

    @Override
    public void onInitialize(@NotNull JavalinConfig config) {
        config.staticFiles.add(staticFileConfig -> {
            staticFileConfig.hostedPath = "/*";
            staticFileConfig.directory = "/static/browser";
            staticFileConfig.location = CLASSPATH;
        });
    }

}
