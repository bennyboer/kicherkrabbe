package de.bennyboer.kicherkrabbe.notifications.api.requests;

import de.bennyboer.kicherkrabbe.notifications.api.ChannelDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class UpdateSystemChannelRequest {

    long version;

    ChannelDTO channel;

}
