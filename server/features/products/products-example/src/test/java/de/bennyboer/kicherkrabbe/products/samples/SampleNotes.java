package de.bennyboer.kicherkrabbe.products.samples;

import de.bennyboer.kicherkrabbe.products.api.NotesDTO;
import lombok.Builder;

@Builder
public class SampleNotes {

    @Builder.Default
    private String contains = "Contains";

    @Builder.Default
    private String care = "Care";

    @Builder.Default
    private String safety = "Safety";

    public NotesDTO toDTO() {
        var dto = new NotesDTO();
        dto.contains = contains;
        dto.care = care;
        dto.safety = safety;
        return dto;
    }

}
