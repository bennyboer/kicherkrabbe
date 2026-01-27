package de.bennyboer.kicherkrabbe.fabrics;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class TopicId {

    String value;

    public static TopicId of(String value) {
        notNull(value, "Topic ID must be given");
        check(!value.isBlank(), "Topic ID must not be blank");

        return new TopicId(value);
    }

    @Override
    public String toString() {
        return "TopicId(%s)".formatted(value);
    }

}
