package de.bennyboer.kicherkrabbe.inquiries;

import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.inquiries.api.InquiryDTO;
import de.bennyboer.kicherkrabbe.inquiries.api.SenderDTO;

public class InquiriesModuleTest {

    private final InquiriesModuleConfig config = new InquiriesModuleConfig();

    private final InquiriesModule module = config.inquiriesModule();

    public void sendInquiry(String requestId, SenderDTO sender, String subject, String message, Agent agent) {
        module.sendInquiry(
                requestId,
                sender,
                subject,
                message,
                agent
        ).block();
    }

    public InquiryDTO getInquiryByRequestId(String requestId, Agent agent) {
        return module.getInquiryByRequestId(requestId, agent).block();
    }

    public void disableSendingInquiries() {
        // TODO
    }

    public void enableSendingInquiries() {
        // TODO
    }

}
