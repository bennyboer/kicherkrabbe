package de.bennyboer.kicherkrabbe.notifications.samples;

import de.bennyboer.kicherkrabbe.notifications.api.requests.SendNotificationRequest;
import lombok.Builder;

@Builder
public class SampleNotification {

    @Builder.Default
    private SampleOrigin origin = SampleOrigin.builder().build();

    @Builder.Default
    private SampleTarget target = SampleTarget.builder().build();

    @Builder.Default
    private String title = "Sample Notification";

    @Builder.Default
    private String message = "Sample notification message";

    public SendNotificationRequest toRequest() {
        var request = new SendNotificationRequest();
        request.origin = origin.toDTO();
        request.target = target.toDTO();
        request.title = title;
        request.message = message;
        return request;
    }

}
