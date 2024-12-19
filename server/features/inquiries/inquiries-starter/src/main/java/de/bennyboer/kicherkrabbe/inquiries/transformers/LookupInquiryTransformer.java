package de.bennyboer.kicherkrabbe.inquiries.transformers;

import de.bennyboer.kicherkrabbe.inquiries.api.InquiryDTO;
import de.bennyboer.kicherkrabbe.inquiries.persistence.lookup.LookupInquiry;

public class LookupInquiryTransformer {

    public static InquiryDTO toApi(LookupInquiry inquiry) {
        var result = new InquiryDTO();

        result.sender = SenderTransformer.toApi(inquiry.getSender());
        result.subject = inquiry.getSubject().getValue();
        result.message = inquiry.getMessage().getValue();
        result.sentAt = inquiry.getCreatedAt();

        return result;
    }

}
