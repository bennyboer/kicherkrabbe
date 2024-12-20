package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.permissions.MissingPermissionError;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EnableDisableTest extends InquiriesModuleTest {

    @Test
    void shouldEnableAndDisableInquiries() {
        // given: the user is allowed to manage inquiries
        allowUserToManageInquiries(loggedInUserId);

        // and: anonymous user is allowed to query status
        allowAnonymousUserToQueryStatusAndSendInquiries();

        // when: enabling inquiries
        enableSendingInquiries();

        // then: the inquiries are enabled
        var status = getStatus(Agent.anonymous());
        assertThat(status.enabled).isTrue();

        // when: disabling inquiries
        disableSendingInquiries();

        // then: the inquiries are disabled
        status = getStatus(Agent.anonymous());
        assertThat(status.enabled).isFalse();
    }

    @Test
    void shouldNotAllowAnonymousUserToEnableOrDisableInquiries() {
        // when: enabling as anonymous user; then: a MissingPermissionException is raised
        assertThatThrownBy(() -> enableSendingInquiries(Agent.anonymous()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);

        // when: disabling as anonymous user; then: a MissingPermissionException is raised
        assertThatThrownBy(() -> disableSendingInquiries(Agent.anonymous()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotAllowSystemUserToEnableOrDisableInquiries() {
        // when: enabling as system user; then: a MissingPermissionException is raised
        assertThatThrownBy(() -> enableSendingInquiries(Agent.system()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);

        // when: disabling as system user; then: a MissingPermissionException is raised
        assertThatThrownBy(() -> disableSendingInquiries(Agent.system()))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

    @Test
    void shouldNotAllowUserToEnableOrDisableInquiriesIfTheyHaveNoPermissionYet() {
        // when: enabling inquiries; then: a MissingPermissionException is raised
        assertThatThrownBy(() -> enableSendingInquiries(Agent.user(AgentId.of(loggedInUserId))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);

        // when: disabling inquiries; then: a MissingPermissionException is raised
        assertThatThrownBy(() -> disableSendingInquiries(Agent.user(AgentId.of(loggedInUserId))))
                .matches(e -> e.getCause() instanceof MissingPermissionError);
    }

}

