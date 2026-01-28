package de.bennyboer.kicherkrabbe.mailbox.mail.unread;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class MarkAsUnreadCmd implements Command {

    public static MarkAsUnreadCmd of() {
        return new MarkAsUnreadCmd();
    }

}
