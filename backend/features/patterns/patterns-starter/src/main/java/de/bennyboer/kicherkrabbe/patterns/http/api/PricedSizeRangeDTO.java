package de.bennyboer.kicherkrabbe.patterns.http.api;

import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.Set;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class PricedSizeRangeDTO {

    long from;

    @Nullable
    Long to;

    @Nullable
    String unit;

    MoneyDTO price;

}
