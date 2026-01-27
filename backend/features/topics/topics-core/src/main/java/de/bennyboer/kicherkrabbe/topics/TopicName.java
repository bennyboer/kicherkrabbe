package de.bennyboer.kicherkrabbe.topics;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class TopicName {

    String value;

    public static TopicName of(String value) {
        notNull(value, "Topic name must be given");
        check(!value.isBlank(), "Topic name must not be blank");

        return new TopicName(value);
    }

    @Override
    public String toString() {
        return "TopicName(%s)".formatted(value);
    }

}
