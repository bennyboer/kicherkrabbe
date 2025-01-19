package de.bennyboer.kicherkrabbe.products.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class FabricCompositionDTO {

    List<FabricCompositionItemDTO> items;

}
