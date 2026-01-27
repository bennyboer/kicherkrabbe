package de.bennyboer.kicherkrabbe.mailing.settings.mailgun.apitoken.clear;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ClearMailgunApiTokenCmd implements Command {

    public static ClearMailgunApiTokenCmd of() {
        return new ClearMailgunApiTokenCmd();
    }

}
