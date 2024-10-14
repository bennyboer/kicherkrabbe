package de.bennyboer.kicherkrabbe.inquiries.api;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class SenderDTO {

    String name;

    String mail;

    @Nullable
    String phone;

}
