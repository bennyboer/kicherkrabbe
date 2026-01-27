package de.bennyboer.kicherkrabbe.notifications.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class SystemSettingsDTO {

    boolean enabled;

    Set<ActivatableChannelDTO> channels;

}
