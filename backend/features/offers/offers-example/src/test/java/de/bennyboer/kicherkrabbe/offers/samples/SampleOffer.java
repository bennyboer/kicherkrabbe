package de.bennyboer.kicherkrabbe.offers.samples;

import de.bennyboer.kicherkrabbe.offers.api.MoneyDTO;
import de.bennyboer.kicherkrabbe.offers.api.NotesDTO;
import lombok.Builder;
import lombok.Singular;

import java.util.List;

@Builder
public class SampleOffer {

    @Builder.Default
    private String productId = "PRODUCT_ID";

    @Singular
    private List<String> images;

    @Builder.Default
    private SampleNotes notes = SampleNotes.builder().build();

    @Builder.Default
    private SamplePrice price = SamplePrice.builder().build();

    public String getProductId() {
        return productId;
    }

    public List<String> getImages() {
        return images.isEmpty() ? List.of("IMAGE_ID") : images;
    }

    public NotesDTO getNotesDTO() {
        return notes.toDTO();
    }

    public MoneyDTO getPriceDTO() {
        return price.toDTO();
    }

}
