package de.bennyboer.kicherkrabbe.notifications.settings.system.enable;

import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import lombok.AllArgsConstructor;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class EnableSystemNotificationsCmd implements Command {

    public static EnableSystemNotificationsCmd of() {
        return new EnableSystemNotificationsCmd();
    }

}
