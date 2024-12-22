package de.bennyboer.kicherkrabbe.mailbox.transformer;

import de.bennyboer.kicherkrabbe.mailbox.api.StatusDTO;
import de.bennyboer.kicherkrabbe.mailbox.mail.Status;

public class StatusTransformer {

    public static Status toInternal(StatusDTO status) {
        return switch (status) {
            case READ -> Status.READ;
            case UNREAD -> Status.UNREAD;
        };
    }

}
