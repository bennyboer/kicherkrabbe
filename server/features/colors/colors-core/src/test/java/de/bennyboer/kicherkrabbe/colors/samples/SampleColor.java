package de.bennyboer.kicherkrabbe.colors.samples;

import de.bennyboer.kicherkrabbe.colors.ColorName;
import lombok.Builder;

@Builder
public class SampleColor {

    @Builder.Default
    private String name = "Blue";

    @Builder.Default
    private int red = 0;

    @Builder.Default
    private int green = 0;

    @Builder.Default
    private int blue = 255;

    public ColorName getName() {
        return ColorName.of(name);
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

}
