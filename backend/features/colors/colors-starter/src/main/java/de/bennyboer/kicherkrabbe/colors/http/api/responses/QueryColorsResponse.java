package de.bennyboer.kicherkrabbe.colors.http.api.responses;

import de.bennyboer.kicherkrabbe.colors.http.api.ColorDTO;
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
