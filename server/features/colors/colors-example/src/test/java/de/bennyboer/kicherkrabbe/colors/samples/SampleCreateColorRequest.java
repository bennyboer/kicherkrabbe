package de.bennyboer.kicherkrabbe.colors.samples;

import de.bennyboer.kicherkrabbe.colors.http.api.requests.CreateColorRequest;
import lombok.Builder;

@Builder
public class SampleCreateColorRequest {

    @Builder.Default
    private String name = "Blue";

    @Builder.Default
    private int red = 0;

    @Builder.Default
    private int green = 0;

    @Builder.Default
    private int blue = 255;

    public CreateColorRequest toRequest() {
        var request = new CreateColorRequest();
        request.name = name;
        request.red = red;
        request.green = green;
        request.blue = blue;
        return request;
    }

}
