package de.bennyboer.kicherkrabbe.notifications.api;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class ChannelDTO {

    ChannelTypeDTO type;

    @Nullable
    String mail;

    @Nullable
    TelegramDTO telegram;

}
