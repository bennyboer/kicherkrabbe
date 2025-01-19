package de.bennyboer.kicherkrabbe.products.api.requests;

import de.bennyboer.kicherkrabbe.products.api.NotesDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class UpdateNotesRequest {

    long version;

    NotesDTO notes;

}
