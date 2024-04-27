package de.bennyboer.kicherkrabbe.users.internal.create;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.users.internal.FullName;
import de.bennyboer.kicherkrabbe.users.internal.Mail;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreateCmd implements Command {

    FullName name;

    Mail mail;

    public static CreateCmd of(FullName name, Mail mail) {
        notNull(name, "Name must be given");
        notNull(mail, "Mail must be given");

        return new CreateCmd(name, mail);
    }

}
