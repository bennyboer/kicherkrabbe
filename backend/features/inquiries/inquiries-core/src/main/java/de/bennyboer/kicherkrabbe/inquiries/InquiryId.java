package de.bennyboer.kicherkrabbe.inquiries;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class InquiryId {

    String value;

    public static InquiryId of(String value) {
        notNull(value, "Inquiry ID must be given");
        check(!value.isBlank(), "Inquiry ID must not be blank");

        return new InquiryId(value);
    }

    public static InquiryId create() {
        return new InquiryId(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "InquiryId(%s)".formatted(value);
    }

}
