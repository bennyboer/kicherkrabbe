package de.bennyboer.kicherkrabbe.notifications.api.requests;

import de.bennyboer.kicherkrabbe.notifications.api.ChannelTypeDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class DeactivateSystemChannelRequest {

    long version;

    ChannelTypeDTO channelType;

}
