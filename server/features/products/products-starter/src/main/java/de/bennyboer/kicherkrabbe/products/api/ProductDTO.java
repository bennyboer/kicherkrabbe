package de.bennyboer.kicherkrabbe.products.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class ProductDTO {

    String id;

    long version;

    String number;

    List<String> images;

    List<LinkDTO> links;

    FabricCompositionDTO fabricComposition;

    NotesDTO notes;

    Instant producedAt;

    Instant createdAt;

}
