package de.bennyboer.kicherkrabbe.patterns.http.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class PatternExtraDTO {

    String name;

    MoneyDTO price;

}
