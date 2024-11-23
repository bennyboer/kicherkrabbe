package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.inquiries.api.InquiryDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.SenderDTO;
import de.bennyboer.kicherkrabbe.testing.time.TestClock;

import java.time.Duration;
import java.time.Instant;

public class InquiriesModuleTest {

    private final TestClock clock = new TestClock();

    private final InquiriesModuleConfig config = new InquiriesModuleConfig();

    private final InquiriesModule module = config.inquiriesModule();

    public void sendInquiry(
            String requestId,
            SenderDTO sender,
            String subject,
            String message,
            Agent agent
    ) {
        module.sendInquiry(
                requestId,
                sender,
                subject,
                message,
                agent,
                "127.0.0.1"
        ).block();
    }

    public void sendInquiry(
            String requestId,
            SenderDTO sender,
            String subject,
            String message,
            Agent agent,
            String ipAddress
    ) {
        module.sendInquiry(
                requestId,
                sender,
                subject,
                message,
                agent,
                ipAddress
        ).block();
    }

    public InquiryDTO getInquiryByRequestId(String requestId, Agent agent) {
        return module.getInquiryByRequestId(requestId, agent).block();
    }

    public void setTime(Instant instant) {
        clock.setNow(instant);
    }

    public void disableSendingInquiries() {
        module.setSendingInquiriesEnabled(false, Agent.system()).block();
    }

    public void enableSendingInquiries() {
        module.setSendingInquiriesEnabled(true, Agent.system()).block();
    }

    public void setMaximumInquiriesPerEmailPerTimeFrame(int count, Duration duration) {
        module.setMaximumInquiriesPerEmailPerTimeFrame(count, duration, Agent.system()).block();
    }

    public void setMaximumInquiriesPerIPAddressPerTimeFrame(int count, Duration duration) {
        module.setMaximumInquiriesPerIPAddressPerTimeFrame(count, duration, Agent.system()).block();
    }

    public void setMaximumInquiriesPerTimeFrame(int count, Duration duration) {
        module.setMaximumInquiriesPerTimeFrame(count, duration, Agent.system()).block();
    }

}
