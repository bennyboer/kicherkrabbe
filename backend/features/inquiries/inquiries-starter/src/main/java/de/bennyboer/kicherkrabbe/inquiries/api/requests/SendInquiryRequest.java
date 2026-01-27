package de.bennyboer.kicherkrabbe.inquiries.api.requests;

import de.bennyboer.kicherkrabbe.inquiries.api.SenderDTO;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class SendInquiryRequest {

    String requestId;

    SenderDTO sender;

    String subject;

    String message;

}
