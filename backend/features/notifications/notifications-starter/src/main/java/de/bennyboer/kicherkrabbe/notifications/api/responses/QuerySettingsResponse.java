package de.bennyboer.kicherkrabbe.notifications.api.responses;

import de.bennyboer.kicherkrabbe.notifications.api.SettingsDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QuerySettingsResponse {

    SettingsDTO settings;

}
