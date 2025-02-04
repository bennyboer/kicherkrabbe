package de.bennyboer.kicherkrabbe.notifications.api.responses;

import de.bennyboer.kicherkrabbe.notifications.api.NotificationDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryNotificationsResponse {

    long total;

    List<NotificationDTO> notifications;

}
