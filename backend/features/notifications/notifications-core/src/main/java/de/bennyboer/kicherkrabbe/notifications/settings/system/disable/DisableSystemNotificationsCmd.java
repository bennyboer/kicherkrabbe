package de.bennyboer.kicherkrabbe.notifications.settings.system.disable;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class DisableSystemNotificationsCmd implements Command {

    public static DisableSystemNotificationsCmd of() {
        return new DisableSystemNotificationsCmd();
    }

}
