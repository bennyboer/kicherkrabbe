package de.bennyboer.kicherkrabbe.patterns;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

@Value
@AllArgsConstructor(access = PRIVATE)
public class PatternAttribution {

    @Nullable
    OriginalPatternName originalPatternName;

    @Nullable
    PatternDesigner designer;

    public static PatternAttribution of(
            @Nullable OriginalPatternName originalPatternName,
            @Nullable PatternDesigner designer
    ) {
        return new PatternAttribution(originalPatternName, designer);
    }

    public static PatternAttribution empty() {
        return new PatternAttribution(null, null);
    }

    @Override

    public String toString() {
        return "PatternAttribution(%s, %s)".formatted(originalPatternName, designer);
    }

    public Optional<OriginalPatternName> getOriginalPatternName() {
        return Optional.ofNullable(originalPatternName);
    }

    public Optional<PatternDesigner> getDesigner() {
        return Optional.ofNullable(designer);
    }

}
