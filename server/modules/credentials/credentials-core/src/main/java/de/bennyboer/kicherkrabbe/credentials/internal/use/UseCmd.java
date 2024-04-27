package de.bennyboer.kicherkrabbe.credentials.internal.use;

import de.bennyboer.kicherkrabbe.credentials.internal.Name;
import de.bennyboer.kicherkrabbe.credentials.internal.Password;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Clock;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class UseCmd implements Command {

    Name name;

    Password password;

    Clock clock;

    public static UseCmd of(Name name, Password password, Clock clock) {
        notNull(name, "Name must be given");
        notNull(password, "Password must be given");
        notNull(clock, "Clock must be given");

        return new UseCmd(name, password, clock);
    }

}
