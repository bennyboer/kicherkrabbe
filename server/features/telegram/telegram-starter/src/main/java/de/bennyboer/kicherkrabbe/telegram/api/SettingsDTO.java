package de.bennyboer.kicherkrabbe.telegram.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class SettingsDTO {

    long version;

    BotSettingsDTO botSettings;

}
