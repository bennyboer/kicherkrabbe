package de.bennyboer.kicherkrabbe.highlights.transformer;

import de.bennyboer.kicherkrabbe.highlights.LinkType;
import de.bennyboer.kicherkrabbe.highlights.api.LinkTypeDTO;

public class LinkTypeTransformer {

    public static LinkTypeDTO toApi(LinkType linkType) {
        return switch (linkType) {
            case PATTERN -> LinkTypeDTO.PATTERN;
            case FABRIC -> LinkTypeDTO.FABRIC;
        };
    }

    public static LinkType toDomain(LinkTypeDTO linkTypeDTO) {
        return switch (linkTypeDTO) {
            case PATTERN -> LinkType.PATTERN;
            case FABRIC -> LinkType.FABRIC;
        };
    }

}
