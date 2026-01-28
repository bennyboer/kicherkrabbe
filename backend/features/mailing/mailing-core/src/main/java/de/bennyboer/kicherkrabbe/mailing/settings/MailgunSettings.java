package de.bennyboer.kicherkrabbe.mailing.settings;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@Value
@With(PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class MailgunSettings {

    @Nullable
    ApiToken apiToken;

    public static MailgunSettings of(@Nullable ApiToken apiToken) {
        return new MailgunSettings(apiToken);
    }

    public static MailgunSettings init() {
        return new MailgunSettings(null);
    }

    public Optional<ApiToken> getApiToken() {
        return Optional.ofNullable(apiToken);
    }

    public MailgunSettings updateApiToken(ApiToken apiToken) {
        return withApiToken(apiToken);
    }

    public MailgunSettings clearApiToken() {
        if (getApiToken().isEmpty()) {
            throw new MailgunApiTokenAlreadyClearedException();
        }

        return withApiToken(null);
    }

}
