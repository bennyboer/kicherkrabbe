package de.bennyboer.kicherkrabbe.assets.image;

import lombok.AllArgsConstructor;
import lombok.Value;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.check;
import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class ImageDimensions {

    int width;

    int height;

    public static ImageDimensions of(int width, int height) {
        check(width > 0, "Width must be positive");
        check(height > 0, "Height must be positive");

        return new ImageDimensions(width, height);
    }

}
