package de.bennyboer.kicherkrabbe.inquiries.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

import static lombok.AccessLevel.PUBLIC;

@ToString
@EqualsAndHashCode
@FieldDefaults(level = PUBLIC)
public class InquiryDTO {

    SenderDTO sender;

    String subject;

    String message;

    Instant sentAt;

}
