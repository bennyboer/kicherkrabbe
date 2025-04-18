package de.bennyboer.kicherkrabbe.inquiries.settings;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static java.util.UUID.randomUUID;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class SettingsId {

    String value;

    public static SettingsId of(String value) {
        notNull(value, "Inquiry settings ID must be given");
        check(!value.isBlank(), "Inquiry settings ID must not be blank");

        return new SettingsId(value);
    }

    public static SettingsId create() {
        return new SettingsId(randomUUID().toString());
    }

    @Override
    public String toString() {
        return "InquirySettingsId(%s)".formatted(value);
    }

}
