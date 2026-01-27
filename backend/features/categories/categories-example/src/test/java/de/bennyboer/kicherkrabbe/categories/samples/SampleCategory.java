package de.bennyboer.kicherkrabbe.categories.samples;

import de.bennyboer.kicherkrabbe.categories.CategoryGroup;
import lombok.Builder;

@Builder
public class SampleCategory {

    @Builder.Default
    private String name = "Sample Category";

    @Builder.Default
    private CategoryGroup group = CategoryGroup.CLOTHING;

    public String getName() {
        return name;
    }

    public CategoryGroup getGroup() {
        return group;
    }

}
