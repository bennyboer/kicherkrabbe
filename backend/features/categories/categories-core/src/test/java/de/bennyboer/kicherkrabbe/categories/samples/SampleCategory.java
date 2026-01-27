package de.bennyboer.kicherkrabbe.categories.samples;

import de.bennyboer.kicherkrabbe.categories.CategoryGroup;
import de.bennyboer.kicherkrabbe.categories.CategoryName;
import lombok.Builder;

@Builder
public class SampleCategory {

    @Builder.Default
    private String name = "Sample Category";

    @Builder.Default
    private CategoryGroup group = CategoryGroup.CLOTHING;

    public CategoryName getName() {
        return CategoryName.of(name);
    }

    public CategoryGroup getGroup() {
        return group;
    }

}
