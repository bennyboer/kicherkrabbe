package de.bennyboer.kicherkrabbe.fabrics.http;

import de.bennyboer.kicherkrabbe.fabrics.FabricKind;
import de.bennyboer.kicherkrabbe.fabrics.http.api.FabricKindDTO;

public class FabricKindTransformer {

    public static FabricKind toFabricKind(FabricKindDTO dto) {
        return switch (dto) {
            case PATTERNED -> FabricKind.PATTERNED;
            case SOLID_COLOR -> FabricKind.SOLID_COLOR;
        };
    }

    public static FabricKindDTO toFabricKindDTO(FabricKind kind) {
        return switch (kind) {
            case PATTERNED -> FabricKindDTO.PATTERNED;
            case SOLID_COLOR -> FabricKindDTO.SOLID_COLOR;
        };
    }

}
