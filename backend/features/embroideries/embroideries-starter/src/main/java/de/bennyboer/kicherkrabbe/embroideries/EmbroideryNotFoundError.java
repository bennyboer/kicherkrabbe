package de.bennyboer.kicherkrabbe.embroideries;

import lombok.Getter;

@Getter
public class EmbroideryNotFoundError extends Exception {

    private final EmbroideryId embroideryId;

    public EmbroideryNotFoundError(EmbroideryId embroideryId) {
        super("Embroidery with ID '%s' not found".formatted(embroideryId.getValue()));
        this.embroideryId = embroideryId;
    }

}
