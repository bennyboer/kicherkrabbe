package de.bennyboer.kicherkrabbe.credentials.samples;

import lombok.Builder;

@Builder
public class SampleCredentials {

    @Builder.Default
    private String name = "john.doe";

    @Builder.Default
    private String password = "password123";

    @Builder.Default
    private String userId = "USER_ID";

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getUserId() {
        return userId;
    }

}
