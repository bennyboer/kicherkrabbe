package de.bennyboer.kicherkrabbe.credentials.internal.create;

import de.bennyboer.kicherkrabbe.credentials.internal.Password;
import de.bennyboer.kicherkrabbe.credentials.internal.Name;
import de.bennyboer.kicherkrabbe.credentials.internal.UserId;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class CreateCmd implements Command {

    Name name;

    Password password;

    UserId userId;

    public static CreateCmd of(Name name, Password password, UserId userId) {
        notNull(name, "Name must be given");
        notNull(password, "Password must be given");
        notNull(userId, "User ID must be given");

        return new CreateCmd(name, password, userId);
    }

}
