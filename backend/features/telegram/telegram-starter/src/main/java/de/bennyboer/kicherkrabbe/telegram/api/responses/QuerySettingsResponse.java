package de.bennyboer.kicherkrabbe.telegram.api.responses;

import de.bennyboer.kicherkrabbe.telegram.api.SettingsDTO;
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
