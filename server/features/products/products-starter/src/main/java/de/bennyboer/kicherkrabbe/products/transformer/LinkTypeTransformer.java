package de.bennyboer.kicherkrabbe.products.transformer;

import de.bennyboer.kicherkrabbe.products.api.LinkTypeDTO;
import de.bennyboer.kicherkrabbe.products.product.LinkType;

public class LinkTypeTransformer {

    public static LinkType toInternal(LinkTypeDTO type) {
        return switch (type) {
            case PATTERN -> LinkType.PATTERN;
            case FABRIC -> LinkType.FABRIC;
        };
    }

    public static LinkTypeDTO toApi(LinkType type) {
        return switch (type) {
            case PATTERN -> LinkTypeDTO.PATTERN;
            case FABRIC -> LinkTypeDTO.FABRIC;
        };
    }
    
}
