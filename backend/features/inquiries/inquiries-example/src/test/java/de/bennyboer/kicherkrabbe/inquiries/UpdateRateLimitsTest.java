package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.inquiries.api.RateLimitDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.RateLimitsDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.requests.UpdateRateLimitsRequest;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UpdateRateLimitsTest extends InquiriesModuleTest {

    @Test
    void shouldUpdateRateLimits() {
        // given: the user is allowed to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // when: updating the rate limits
        var request = new UpdateRateLimitsRequest();
        request.rateLimits = new RateLimitsDTO();
        request.rateLimits.perMail = new RateLimitDTO();
        request.rateLimits.perMail.maxRequests = 3;
        request.rateLimits.perMail.duration = Duration.ofHours(24);
        request.rateLimits.perIp = new RateLimitDTO();
        request.rateLimits.perIp.maxRequests = 5;
        request.rateLimits.perIp.duration = Duration.ofHours(48);
        request.rateLimits.overall = new RateLimitDTO();
        request.rateLimits.overall.maxRequests = 100;
        request.rateLimits.overall.duration = Duration.ofHours(72);
        updateRateLimits(request, Agent.user(AgentId.of(loggedInUserId)));

        // then: the rate limits are correct
        var settings = getSettings(Agent.user(AgentId.of(loggedInUserId)));
        assertThat(settings.rateLimits.perMail.maxRequests).isEqualTo(3);
        assertThat(settings.rateLimits.perMail.duration).isEqualTo(Duration.ofHours(24));
        assertThat(settings.rateLimits.perIp.maxRequests).isEqualTo(5);
        assertThat(settings.rateLimits.perIp.duration).isEqualTo(Duration.ofHours(48));
        assertThat(settings.rateLimits.overall.maxRequests).isEqualTo(100);
        assertThat(settings.rateLimits.overall.duration).isEqualTo(Duration.ofHours(72));
    }

    @Test
    void shouldNotAllowAnonymousUserToUpdateRateLimits() {
        // when: updating rate limits as anonymous user; then: a MissingPermissionException is raised
        var request = new UpdateRateLimitsRequest();
        request.rateLimits = new RateLimitsDTO();
        request.rateLimits.perMail = new RateLimitDTO();
        request.rateLimits.perMail.maxRequests = 3;
        request.rateLimits.perMail.duration = Duration.ofHours(24);
        request.rateLimits.perIp = new RateLimitDTO();
        request.rateLimits.perIp.maxRequests = 5;
        request.rateLimits.perIp.duration = Duration.ofHours(48);
        request.rateLimits.overall = new RateLimitDTO();
        request.rateLimits.overall.maxRequests = 100;
        request.rateLimits.overall.duration = Duration.ofHours(72);
        assertThatThrownBy(() -> updateRateLimits(request, Agent.anonymous()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotAllowSystemUserToUpdateRateLimits() {
        // when: updating rate limits as system user; then: a MissingPermissionException is raised
        var request = new UpdateRateLimitsRequest();
        request.rateLimits = new RateLimitsDTO();
        request.rateLimits.perMail = new RateLimitDTO();
        request.rateLimits.perMail.maxRequests = 3;
        request.rateLimits.perMail.duration = Duration.ofHours(24);
        request.rateLimits.perIp = new RateLimitDTO();
        request.rateLimits.perIp.maxRequests = 5;
        request.rateLimits.perIp.duration = Duration.ofHours(48);
        request.rateLimits.overall = new RateLimitDTO();
        request.rateLimits.overall.maxRequests = 100;
        request.rateLimits.overall.duration = Duration.ofHours(72);
        assertThatThrownBy(() -> updateRateLimits(request, Agent.system()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotAllowUserToUpdateRateLimitsIfTheyHaveNoPermissionYet() {
        // when: updating rate limits as user; then: a MissingPermissionException is raised
        var request = new UpdateRateLimitsRequest();
        request.rateLimits = new RateLimitsDTO();
        request.rateLimits.perMail = new RateLimitDTO();
        request.rateLimits.perMail.maxRequests = 3;
        request.rateLimits.perMail.duration = Duration.ofHours(24);
        request.rateLimits.perIp = new RateLimitDTO();
        request.rateLimits.perIp.maxRequests = 5;
        request.rateLimits.perIp.duration = Duration.ofHours(48);
        request.rateLimits.overall = new RateLimitDTO();
        request.rateLimits.overall.maxRequests = 100;
        request.rateLimits.overall.duration = Duration.ofHours(72);
        assertThatThrownBy(() -> updateRateLimits(request, Agent.user(AgentId.of(loggedInUserId))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}

