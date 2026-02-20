package de.bennyboer.kicherkrabbe.offers.samples;

import de.bennyboer.kicherkrabbe.offers.api.NotesDTO;
import lombok.Builder;

@Builder
public class SampleNotes {

    @Builder.Default
    private String description = "Sample description";

    private String contains;

    private String care;

    private String safety;

    public NotesDTO toDTO() {
        var dto = new NotesDTO();
        dto.description = description;
        dto.contains = contains;
        dto.care = care;
        dto.safety = safety;
        return dto;
    }

}
