package de.bennyboer.kicherkrabbe.embroideries.samples;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.Set;

@Getter
@Builder
public class SampleEmbroidery {

    @Builder.Default
    private String name = "Sample Embroidery";

    @Builder.Default
    private String image = "IMAGE_ID";

    @Singular
    private Set<String> categories;

}
