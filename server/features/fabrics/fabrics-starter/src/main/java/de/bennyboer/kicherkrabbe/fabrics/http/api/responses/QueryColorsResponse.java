package de.bennyboer.kicherkrabbe.fabrics.http.api.responses;

import de.bennyboer.kicherkrabbe.fabrics.http.api.ColorDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class QueryColorsResponse {

    public List<ColorDTO> colors;

}
