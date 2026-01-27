package de.bennyboer.kicherkrabbe.categories.samples;

import de.bennyboer.kicherkrabbe.categories.http.api.CategoryGroupDTO;
import de.bennyboer.kicherkrabbe.categories.http.api.requests.CreateCategoryRequest;
import lombok.Builder;

@Builder
public class SampleCreateCategoryRequest {

    @Builder.Default
    private String name = "Sample Category";

    @Builder.Default
    private CategoryGroupDTO group = CategoryGroupDTO.CLOTHING;

    public CreateCategoryRequest toRequest() {
        var request = new CreateCategoryRequest();
        request.name = name;
        request.group = group;
        return request;
    }

}
