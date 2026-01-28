package de.bennyboer.kicherkrabbe.notifications.api.requests;

import de.bennyboer.kicherkrabbe.notifications.api.OriginDTO;
import de.bennyboer.kicherkrabbe.notifications.api.TargetDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class SendNotificationRequest {

    OriginDTO origin;

    TargetDTO target;

    String title;

    String message;

}
