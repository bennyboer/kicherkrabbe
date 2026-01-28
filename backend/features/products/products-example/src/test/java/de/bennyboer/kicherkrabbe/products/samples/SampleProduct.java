package de.bennyboer.kicherkrabbe.products.samples;

import de.bennyboer.kicherkrabbe.products.api.FabricCompositionDTO;
import de.bennyboer.kicherkrabbe.products.api.LinkDTO;
import de.bennyboer.kicherkrabbe.products.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.products.api.NotesDTO;
import de.bennyboer.kicherkrabbe.products.api.requests.CreateProductRequest;
import lombok.Builder;
import lombok.Singular;

import java.time.Instant;
import java.util.List;

@Builder
public class SampleProduct {

    @Singular
    private List<String> images;

    @Singular
    private List<SampleLink> links;

    @Builder.Default
    private SampleFabricComposition fabricComposition = SampleFabricComposition.builder().build();

    @Builder.Default
    private SampleNotes notes = SampleNotes.builder().build();

    @Builder.Default
    private Instant producedAt = Instant.parse("2024-11-08T12:30:00.000Z");

    public List<String> getImages() {
        return images.isEmpty() ? List.of("IMAGE_ID_1", "IMAGE_ID_2") : images;
    }

    public List<LinkDTO> getLinkDTOs() {
        if (links.isEmpty()) {
            return List.of(
                    SampleLink.builder()
                            .type(LinkTypeDTO.PATTERN)
                            .id("PATTERN_ID_1")
                            .name("Pattern 1")
                            .build()
                            .toDTO(),
                    SampleLink.builder()
                            .type(LinkTypeDTO.FABRIC)
                            .id("FABRIC_ID_1")
                            .name("Fabric 1")
                            .build()
                            .toDTO()
            );
        }
        return links.stream().map(SampleLink::toDTO).toList();
    }

    public FabricCompositionDTO getFabricCompositionDTO() {
        return fabricComposition.toDTO();
    }

    public NotesDTO getNotesDTO() {
        return notes.toDTO();
    }

    public Instant getProducedAt() {
        return producedAt;
    }

    public CreateProductRequest toRequest() {
        var request = new CreateProductRequest();
        request.images = getImages();
        request.links = getLinkDTOs();
        request.fabricComposition = getFabricCompositionDTO();
        request.notes = getNotesDTO();
        request.producedAt = producedAt;
        return request;
    }

}
