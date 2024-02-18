package de.bennyboer.kicherkrabbe.app;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import de.bennyboer.kicherkrabbe.app.plugins.frontend.FrontendPlugin;
import de.bennyboer.kicherkrabbe.app.util.files.FileWatcher;
import de.bennyboer.kicherkrabbe.app.util.files.FileWatcherFactory;
import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import io.javalin.community.ssl.TlsConfig;
import io.javalin.config.JavalinConfig;
import io.javalin.json.JavalinJackson;
import io.javalin.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

@Slf4j
public class App {

    private final AppConfig appConfig;

    private final JsonMapper jsonMapper;

    public App(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.jsonMapper = createJsonMapper();
    }

    public static void main(String[] args) {
        AppConfig config = loadConfig();

        App app = new App(config);
        Javalin javalin = app.setupJavalin();
        javalin.start(config.getHost(), config.getPort());
    }

    private static AppConfig loadConfig() {
        File configFile = new File("config.properties");
        if (!configFile.exists()) {
            return AppConfig.builder().build();
        }

        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            log.error("Failed to load config file", e);
            System.exit(1);
        }

        boolean isSecure = Boolean.parseBoolean(properties.getProperty("secure", "true"));
        String host = properties.getProperty("host", "0.0.0.0");
        int port = Integer.parseInt(properties.getProperty("port", "443"));
        Profile profile = switch (properties.getProperty("profile", "prod")) {
            case "dev" -> Profile.DEVELOPMENT;
            case "prod" -> Profile.PRODUCTION;
            default -> throw new IllegalArgumentException("Invalid profile configuration");
        };
        String certPath = properties.getProperty("cert-path");
        String keyPath = properties.getProperty("key-path");

        return AppConfig.builder()
                .isSecure(isSecure)
                .host(host)
                .port(port)
                .profile(profile)
                .certPath(certPath)
                .keyPath(keyPath)
                .build();
    }

    private Javalin setupJavalin() {
        return Javalin.create(config -> {
            config.http.maxRequestSize = 16 * 1024 * 1024;
            config.useVirtualThreads = true;
            config.jsonMapper(jsonMapper);

            setupCors(config);
            setupHttps(config);
            setupFrontend(config);
            setupApi(config);

            setupServerEventHandlers(config);
        });
    }

    private void setupApi(JavalinConfig config) {
        config.router.apiBuilder(() -> {
            path("api", () -> {
                get("/", ctx -> ctx.result("Hello World"));
            });
        });
    }

    private void setupFrontend(JavalinConfig config) {
        if (appConfig.isProductionProfile()) {
            config.registerPlugin(new FrontendPlugin());
        }
    }

    private void setupCors(JavalinConfig config) {
        if (appConfig.isDevelopmentProfile()) {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(corsConfig -> {
                    corsConfig.anyHost();
                    corsConfig.exposeHeader("Location");
                });
            });
        }
    }

    private void setupHttps(JavalinConfig config) {
        if (appConfig.isSecure()) {
            config.bundledPlugins.enableSslRedirects(); // Will redirect incoming traffic on port 80 to 443

            Path certPath = Path.of(appConfig.getCertPath().orElseThrow());
            Path keyPath = Path.of(appConfig.getKeyPath().orElseThrow());

            SslPlugin plugin = new SslPlugin(conf -> {
                conf.insecure = true;
                conf.secure = true;
                conf.http2 = true;
                conf.redirect = true;
                conf.tlsConfig = TlsConfig.MODERN;
                conf.pemFromPath(
                        certPath.toAbsolutePath().toString(),
                        keyPath.toAbsolutePath().toString()
                );
            });
            config.registerPlugin(plugin);

            FileWatcher fileWatcher = FileWatcherFactory.getInstance().createWatcher(certPath, () -> {
                log.info("Certificate file changed, reloading HTTPS configuration");

                plugin.reload(conf -> {
                    conf.pemFromPath(
                            certPath.toAbsolutePath().toString(),
                            keyPath.toAbsolutePath().toString()
                    );
                });
            });
            fileWatcher.start();
        }
    }

    private JsonMapper createJsonMapper() {
        return new JavalinJackson().updateMapper(mapper -> {
            mapper.registerModule(new Jdk8Module());
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        });
    }

    private void setupServerEventHandlers(JavalinConfig config) {
        config.events(event -> event.serverStopping(this::onServerStopping));
    }

    private void onServerStopping() {
        stopFileWatchers();
    }

    private void stopFileWatchers() {
        FileWatcherFactory.getInstance().stopAll();
    }

}
