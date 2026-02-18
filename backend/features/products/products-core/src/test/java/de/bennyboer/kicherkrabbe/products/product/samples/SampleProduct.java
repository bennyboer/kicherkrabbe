package de.bennyboer.kicherkrabbe.products.product.samples;

import de.bennyboer.kicherkrabbe.products.product.*;
import lombok.Builder;
import lombok.Singular;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class SampleProduct {

    @Builder.Default
    private String number = "2024-1";

    @Singular
    private List<String> imageIds;

    @Singular
    private Set<SampleLink> links;

    @Singular
    private Set<SampleFabricCompositionItem> compositionItems;

    @Builder.Default
    private String containsNote = "Contains sample stuff.";

    @Builder.Default
    private String careNote = "Handle with care.";

    @Builder.Default
    private String safetyNote = "Keep away from fire.";

    @Builder.Default
    private Instant producedAt = Instant.parse("2024-12-10T12:30:00.00Z");

    public ProductNumber getNumber() {
        return ProductNumber.of(number);
    }

    public List<ImageId> getImageIds() {
        if (imageIds.isEmpty()) {
            return List.of(ImageId.of("IMAGE_ID"));
        }
        return imageIds.stream()
                .map(ImageId::of)
                .toList();
    }

    public Links getLinks() {
        if (links.isEmpty()) {
            return Links.of(Set.of(
                    SampleLink.builder().type(LinkType.PATTERN).id("PATTERN_ID").name("Pattern").build().toValue()
            ));
        }
        return Links.of(links.stream()
                .map(SampleLink::toValue)
                .collect(Collectors.toSet()));
    }

    public FabricComposition getFabricComposition() {
        if (compositionItems.isEmpty()) {
            return FabricComposition.of(Set.of(
                    SampleFabricCompositionItem.builder().build().toValue()
            ));
        }
        return FabricComposition.of(compositionItems.stream()
                .map(SampleFabricCompositionItem::toValue)
                .collect(Collectors.toSet()));
    }

    public Notes getNotes() {
        return Notes.of(
                Note.of(containsNote),
                Note.of(careNote),
                Note.of(safetyNote)
        );
    }

    public Instant getProducedAt() {
        return producedAt;
    }

}
