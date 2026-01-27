package de.bennyboer.kicherkrabbe.credentials.samples;

import de.bennyboer.kicherkrabbe.credentials.http.api.requests.UseCredentialsRequest;
import lombok.Builder;

@Builder
public class SampleUseCredentialsRequest {

    @Builder.Default
    private String name = "john.doe";

    @Builder.Default
    private String password = "password123";

    public UseCredentialsRequest toRequest() {
        var request = new UseCredentialsRequest();
        request.name = name;
        request.password = password;
        return request;
    }

}
