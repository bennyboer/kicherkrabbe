package de.bennyboer.kicherkrabbe.users.samples;

import lombok.Builder;

@Builder
public class SampleUser {

    @Builder.Default
    private String firstName = "John";

    @Builder.Default
    private String lastName = "Doe";

    @Builder.Default
    private String mail = "john.doe@example.com";

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMail() {
        return mail;
    }

}
