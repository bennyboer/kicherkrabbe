package de.bennyboer.kicherkrabbe.fabrics;

import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class ColorsMissingError extends Exception {

    private final Set<ColorId> missingColors;

    public ColorsMissingError(Set<ColorId> missingColors) {
        super("Colors are missing: " + missingColors.stream()
                .map(ColorId::getValue)
                .collect(Collectors.joining(", ")));

        this.missingColors = missingColors;
    }

}
