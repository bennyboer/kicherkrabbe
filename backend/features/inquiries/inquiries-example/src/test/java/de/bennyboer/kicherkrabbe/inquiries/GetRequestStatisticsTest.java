package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.inquiries.api.SenderDTO;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GetRequestStatisticsTest extends InquiriesModuleTest {

    @Test
    void shouldRetrieveRequestStatisticsAsUser() {
        // given: the user is allowed to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: anonymous user is allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: sending inquiries is enabled
        enableSendingInquiries();

        // and: the time is fixed to a specific point in time
        clock.setNow(Instant.parse("2024-12-03T12:30:00Z"));

        // when: querying the statistics
        var statistics = getRequestStatistics(
                clock.instant().minus(7, ChronoUnit.DAYS),
                clock.instant(),
                Agent.user(AgentId.of(loggedInUserId))
        );

        // then: the statistics are empty
        assertThat(statistics.statistics).isEmpty();

        // when: sending some inquiries
        var sender = new SenderDTO();
        sender.name = "John Doe";
        sender.mail = "john.doe+test@example.com";
        sender.phone = "+49 1234 5678 9999";

        sendInquiry(
                "REQUEST_ID_1",
                sender,
                "Some subject",
                "Some message",
                Agent.anonymous()
        );
        sendInquiry(
                "REQUEST_ID_2",
                sender,
                "Some subject",
                "Some message",
                Agent.anonymous()
        );

        // and: sending some more a little later
        clock.setNow(clock.instant().plus(3, ChronoUnit.DAYS));
        sendInquiry(
                "REQUEST_ID_3",
                sender,
                "Some subject",
                "Some message",
                Agent.anonymous()
        );

        // when: querying the statistics
        statistics = getRequestStatistics(
                clock.instant().minus(7, ChronoUnit.DAYS),
                clock.instant().plus(1, ChronoUnit.DAYS),
                Agent.user(AgentId.of(loggedInUserId))
        );

        // then: the statistics are as expected
        assertThat(statistics.statistics).hasSize(2);

        var firstDay = statistics.statistics.get(0);
        assertThat(firstDay.dateRange.from).isEqualTo(Instant.parse("2024-12-03T00:00:00Z"));
        assertThat(firstDay.dateRange.to).isEqualTo(Instant.parse("2024-12-04T00:00:00Z"));
        assertThat(firstDay.totalRequests).isEqualTo(2);

        var secondDay = statistics.statistics.get(1);
        assertThat(secondDay.dateRange.from).isEqualTo(Instant.parse("2024-12-06T00:00:00Z"));
        assertThat(secondDay.dateRange.to).isEqualTo(Instant.parse("2024-12-07T00:00:00Z"));
        assertThat(secondDay.totalRequests).isEqualTo(1);
    }

    @Test
    void shouldNotAllowAnonymousUserToQuerySettings() {
        // when: querying the statistics as anonymous user; then: a MissingPermissionException is raised
        assertThatThrownBy(() -> getRequestStatistics(
                clock.instant().minus(7, ChronoUnit.DAYS),
                clock.instant(),
                Agent.anonymous()
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotAllowSystemUserToQuerySettings() {
        // when: querying the statistics as system user; then: a MissingPermissionException is raised
        assertThatThrownBy(() -> getRequestStatistics(
                clock.instant().minus(7, ChronoUnit.DAYS),
                clock.instant(),
                Agent.system()
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotAllowUserToQuerySettingsIfTheyHaveNoPermissionYet() {
        // when: querying the statistics as user; then: a MissingPermissionException is raised
        assertThatThrownBy(() -> getRequestStatistics(
                clock.instant().minus(7, ChronoUnit.DAYS),
                clock.instant(),
                Agent.user(AgentId.of(loggedInUserId))
        )).matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}

