package de.bennyboer.kicherkrabbe.mailing.settings.init;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.mailing.settings.MailgunSettings;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class InitCmd implements Command {

    MailgunSettings mailgun;

    public static InitCmd of(MailgunSettings mailgun) {
        notNull(mailgun, "Mailgun settings must be given");

        return new InitCmd(mailgun);
    }

}
