package de.bennyboer.kicherkrabbe.mailing.settings.init;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.mailing.settings.MailgunSettings;
import de.bennyboer.kicherkrabbe.mailing.settings.RateLimitSettings;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class InitCmd implements Command {

    RateLimitSettings rateLimit;

    MailgunSettings mailgun;

    public static InitCmd of(RateLimitSettings rateLimit, MailgunSettings mailgun) {
        notNull(rateLimit, "Rate limit settings must be given");
        notNull(mailgun, "Mailgun settings must be given");

        return new InitCmd(rateLimit, mailgun);
    }

}
