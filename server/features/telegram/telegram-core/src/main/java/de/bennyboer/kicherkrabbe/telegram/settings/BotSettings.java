package de.bennyboer.kicherkrabbe.telegram.settings;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class BotSettings {

    @Nullable
    ApiToken apiToken;

    public static BotSettings of(@Nullable ApiToken apiToken) {
        return new BotSettings(apiToken);
    }

    public static BotSettings init() {
        return of(null);
    }

    @Override
    public String toString() {
        return "BotSettings(apiToken=%s)".formatted("********");
    }

    public Optional<ApiToken> getApiToken() {
        return Optional.ofNullable(apiToken);
    }

    public BotSettings updateApiToken(ApiToken apiToken) {
        return withApiToken(apiToken);
    }

    public BotSettings clearApiToken() {
        if (getApiToken().isEmpty()) {
            throw new BotApiTokenAlreadyClearedException();
        }

        return withApiToken(null);
    }

}
