package de.bennyboer.kicherkrabbe.colors.http.responses;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryColorsResponse {

    long skip;

    long limit;

    long total;

    List<ColorDTO> colors;

}
