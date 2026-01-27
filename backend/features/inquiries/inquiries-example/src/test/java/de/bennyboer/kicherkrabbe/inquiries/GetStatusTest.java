package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GetStatusTest extends InquiriesModuleTest {

    @Test
    void shouldRetrieveStatusAsAnonymousUser() {
        // given: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // and: sending inquiries is enabled
        allowUserToManageInquiries(loggedInUserId);
        enableSendingInquiries();

        // when: querying the status
        var status = getStatus(Agent.anonymous());

        // then: the status is enabled
        assertThat(status.enabled).isTrue();

        // when: sending inquiries is disabled
        disableSendingInquiries();

        // when: querying the status
        status = getStatus(Agent.anonymous());

        // then: the status is disabled
        assertThat(status.enabled).isFalse();
    }

    @Test
    void shouldNotAllowAnonymousUserToQueryStatusIfPermissionIsNotYetGiven() {
        // given: sending inquiries is enabled
        allowUserToManageInquiries(loggedInUserId);
        enableSendingInquiries();

        // when: querying the status as anonymous user; then: a MissingPermissionException is raised
        assertThatThrownBy(() -> getStatus(Agent.anonymous()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotAllowQueryingStatusAsSystemUser() {
        // and: anonymous users are allowed to send inquiries
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // given: sending inquiries is enabled
        allowUserToManageInquiries(loggedInUserId);
        enableSendingInquiries();

        // when: querying the status as system user; then: a MissingPermissionException is raised
        assertThatThrownBy(() -> getStatus(Agent.system()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotAllowQueryingStatusAsUser() {
        // given: sending inquiries is enabled
        allowUserToManageInquiries(loggedInUserId);
        enableSendingInquiries();

        // when: querying the status as user; then: a MissingPermissionException is raised
        assertThatThrownBy(() -> getStatus(Agent.user(AgentId.of(loggedInUserId))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}

