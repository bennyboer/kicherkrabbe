package de.bennyboer.kicherkrabbe.inquiries;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class RequestId {

    String value;

    public static RequestId of(String value) {
        notNull(value, "Request ID must be given");
        check(!value.isBlank(), "Request ID must not be blank");

        return new RequestId(value);
    }

    @Override
    public String toString() {
        return "RequestId(%s)".formatted(value);
    }

}
