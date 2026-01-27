package de.bennyboer.kicherkrabbe.mailbox.mail.read;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class MarkAsReadCmd implements Command {

    public static MarkAsReadCmd of() {
        return new MarkAsReadCmd();
    }

}
