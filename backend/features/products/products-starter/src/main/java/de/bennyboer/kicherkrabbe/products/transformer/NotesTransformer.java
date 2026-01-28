package de.bennyboer.kicherkrabbe.products.transformer;

import de.bennyboer.kicherkrabbe.products.api.NotesDTO;
import de.bennyboer.kicherkrabbe.products.product.Note;
import de.bennyboer.kicherkrabbe.products.product.Notes;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

public class NotesTransformer {

    public static Notes toInternal(NotesDTO notes) {
        notNull(notes, "Notes must be given");

        var contains = Note.of(notes.contains);
        var care = Note.of(notes.care);
        var safety = Note.of(notes.safety);

        return Notes.of(contains, care, safety);
    }

    public static NotesDTO toApi(Notes notes) {
        var result = new NotesDTO();

        result.contains = notes.getContains().getValue();
        result.care = notes.getCare().getValue();
        result.safety = notes.getSafety().getValue();

        return result;
    }

}
