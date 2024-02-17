package de.bennyboer.kicherkrabbe.app;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import de.bennyboer.kicherkrabbe.app.plugins.frontend.FrontendPlugin;
import io.javalin.Javalin;
import io.javalin.community.ssl.SslPlugin;
import io.javalin.community.ssl.TlsConfig;
import io.javalin.config.JavalinConfig;
import io.javalin.json.JavalinJackson;
import io.javalin.json.JsonMapper;

import java.util.Arrays;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class App {

    private final AppConfig appConfig;

    private final JsonMapper jsonMapper;

    public App(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.jsonMapper = createJsonMapper();
    }

    public static void main(String[] args) {
        Profile activeProfile = Arrays.stream(args)
                .findFirst()
                .filter(v -> v.equalsIgnoreCase("dev"))
                .map(v -> Profile.DEVELOPMENT)
                .orElse(Profile.PRODUCTION);

        AppConfig config = AppConfig.builder()
                .isSecure(activeProfile.isProduction())
                .profile(activeProfile)
                .build();

        App app = new App(config);
        Javalin javalin = app.setupJavalin();
        javalin.start(config.getHost(), config.getPort());
    }

    private Javalin setupJavalin() {
        return Javalin.create(config -> {
            config.http.maxRequestSize = 16 * 1024 * 1024;
            config.useVirtualThreads = true;
            config.jsonMapper(jsonMapper);

            setupCors(config);
            setupSsl(config);
            setupFrontend(config);
            setupApi(config);
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

    private void setupSsl(JavalinConfig config) {
        if (appConfig.isSecure()) {
            config.registerPlugin(new SslPlugin(conf -> {
                conf.insecure = false;
                conf.secure = true;
                conf.http2 = true;
                conf.redirect = true;
                conf.tlsConfig = TlsConfig.MODERN;
                conf.pemFromClasspath(
                        "/keys/cert.pem",
                        "/keys/key.pem",
                        "password"
                );
                // TODO Generate cert and private key file using openssh and provide passwort - via config!
                // TODO Use FileWatcher to reload certificate after change
            }));
        }
    }

    private JsonMapper createJsonMapper() {
        return new JavalinJackson().updateMapper(mapper -> {
            mapper.registerModule(new Jdk8Module());
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        });
    }

}
