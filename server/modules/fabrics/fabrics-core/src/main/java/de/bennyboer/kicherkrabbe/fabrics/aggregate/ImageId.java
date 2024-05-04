package de.bennyboer.kicherkrabbe.fabrics.aggregate;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ImageId {

    String value;

    public static ImageId of(String value) {
        notNull(value, "Image ID must be given");
        check(!value.isBlank(), "Image ID must not be blank");

        return new ImageId(value);
    }

    @Override
    public String toString() {
        return "ImageId(%s)".formatted(value);
    }

}
