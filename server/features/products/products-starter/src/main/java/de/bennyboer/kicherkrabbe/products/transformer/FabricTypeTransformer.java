package de.bennyboer.kicherkrabbe.products.transformer;

import de.bennyboer.kicherkrabbe.products.api.FabricTypeDTO;
import de.bennyboer.kicherkrabbe.products.product.FabricType;

import static de.bennyboer.kicherkrabbe.commons.Preconditions.notNull;

public class FabricTypeTransformer {

    public static FabricType toInternal(FabricTypeDTO type) {
        notNull(type, "FabricType must be given");

        return switch (type) {
            case ABACA -> FabricType.ABACA;
            case ALFA -> FabricType.ALFA;
            case BAMBOO -> FabricType.BAMBOO;
            case HEMP -> FabricType.HEMP;
            case COTTON -> FabricType.COTTON;
            case COCONUT -> FabricType.COCONUT;
            case CASHMERE -> FabricType.CASHMERE;
            case HENEQUEN -> FabricType.HENEQUEN;
            case HALF_LINEN -> FabricType.HALF_LINEN;
            case JUTE -> FabricType.JUTE;
            case KENAF -> FabricType.KENAF;
            case KAPOK -> FabricType.KAPOK;
            case LINEN -> FabricType.LINEN;
            case MAGUEY -> FabricType.MAGUEY;
            case RAMIE -> FabricType.RAMIE;
            case SISAL -> FabricType.SISAL;
            case SUNN -> FabricType.SUNN;
            case CELLULOSE_ACETATE -> FabricType.CELLULOSE_ACETATE;
            case CUPRO -> FabricType.CUPRO;
            case LYOCELL -> FabricType.LYOCELL;
            case MODAL -> FabricType.MODAL;
            case PAPER -> FabricType.PAPER;
            case TRIACETATE -> FabricType.TRIACETATE;
            case VISCOSE -> FabricType.VISCOSE;
            case ARAMID -> FabricType.ARAMID;
            case CARBON_FIBER -> FabricType.CARBON_FIBER;
            case CHLORO_FIBER -> FabricType.CHLORO_FIBER;
            case ELASTANE -> FabricType.ELASTANE;
            case FLUOR_FIBER -> FabricType.FLUOR_FIBER;
            case LUREX -> FabricType.LUREX;
            case MODACRYLIC -> FabricType.MODACRYLIC;
            case NYLON -> FabricType.NYLON;
            case POLYAMIDE -> FabricType.POLYAMIDE;
            case POLYCARBAMIDE -> FabricType.POLYCARBAMIDE;
            case ACRYLIC -> FabricType.ACRYLIC;
            case POLYETHYLENE -> FabricType.POLYETHYLENE;
            case POLYESTER -> FabricType.POLYESTER;
            case POLYPROPYLENE -> FabricType.POLYPROPYLENE;
            case POLYURETHANE -> FabricType.POLYURETHANE;
            case POLYVINYL_CHLORIDE -> FabricType.POLYVINYL_CHLORIDE;
            case TETORON_COTTON -> FabricType.TETORON_COTTON;
            case TRIVINYL -> FabricType.TRIVINYL;
            case VINYL -> FabricType.VINYL;
            case HAIR -> FabricType.HAIR;
            case COW_HAIR -> FabricType.COW_HAIR;
            case HORSE_HAIR -> FabricType.HORSE_HAIR;
            case GOAT_HAIR -> FabricType.GOAT_HAIR;
            case SILK -> FabricType.SILK;
            case ANGORA_WOOL -> FabricType.ANGORA_WOOL;
            case BEAVER -> FabricType.BEAVER;
            case CASHGORA_GOAT -> FabricType.CASHGORA_GOAT;
            case CAMEL -> FabricType.CAMEL;
            case LAMA -> FabricType.LAMA;
            case ANGORA_GOAT -> FabricType.ANGORA_GOAT;
            case WOOL -> FabricType.WOOL;
            case ALPAKA -> FabricType.ALPAKA;
            case OTTER -> FabricType.OTTER;
            case VIRGIN_WOOL -> FabricType.VIRGIN_WOOL;
            case YAK -> FabricType.YAK;
            case UNKNOWN -> FabricType.UNKNOWN;
        };
    }

    public static FabricTypeDTO toApi(FabricType type) {
        return switch (type) {
            case ABACA -> FabricTypeDTO.ABACA;
            case ALFA -> FabricTypeDTO.ALFA;
            case BAMBOO -> FabricTypeDTO.BAMBOO;
            case HEMP -> FabricTypeDTO.HEMP;
            case COTTON -> FabricTypeDTO.COTTON;
            case COCONUT -> FabricTypeDTO.COCONUT;
            case CASHMERE -> FabricTypeDTO.CASHMERE;
            case HENEQUEN -> FabricTypeDTO.HENEQUEN;
            case HALF_LINEN -> FabricTypeDTO.HALF_LINEN;
            case JUTE -> FabricTypeDTO.JUTE;
            case KENAF -> FabricTypeDTO.KENAF;
            case KAPOK -> FabricTypeDTO.KAPOK;
            case LINEN -> FabricTypeDTO.LINEN;
            case MAGUEY -> FabricTypeDTO.MAGUEY;
            case RAMIE -> FabricTypeDTO.RAMIE;
            case SISAL -> FabricTypeDTO.SISAL;
            case SUNN -> FabricTypeDTO.SUNN;
            case CELLULOSE_ACETATE -> FabricTypeDTO.CELLULOSE_ACETATE;
            case CUPRO -> FabricTypeDTO.CUPRO;
            case LYOCELL -> FabricTypeDTO.LYOCELL;
            case MODAL -> FabricTypeDTO.MODAL;
            case PAPER -> FabricTypeDTO.PAPER;
            case TRIACETATE -> FabricTypeDTO.TRIACETATE;
            case VISCOSE -> FabricTypeDTO.VISCOSE;
            case ARAMID -> FabricTypeDTO.ARAMID;
            case CARBON_FIBER -> FabricTypeDTO.CARBON_FIBER;
            case CHLORO_FIBER -> FabricTypeDTO.CHLORO_FIBER;
            case ELASTANE -> FabricTypeDTO.ELASTANE;
            case FLUOR_FIBER -> FabricTypeDTO.FLUOR_FIBER;
            case LUREX -> FabricTypeDTO.LUREX;
            case MODACRYLIC -> FabricTypeDTO.MODACRYLIC;
            case NYLON -> FabricTypeDTO.NYLON;
            case POLYAMIDE -> FabricTypeDTO.POLYAMIDE;
            case POLYCARBAMIDE -> FabricTypeDTO.POLYCARBAMIDE;
            case ACRYLIC -> FabricTypeDTO.ACRYLIC;
            case POLYETHYLENE -> FabricTypeDTO.POLYETHYLENE;
            case POLYESTER -> FabricTypeDTO.POLYESTER;
            case POLYPROPYLENE -> FabricTypeDTO.POLYPROPYLENE;
            case POLYURETHANE -> FabricTypeDTO.POLYURETHANE;
            case POLYVINYL_CHLORIDE -> FabricTypeDTO.POLYVINYL_CHLORIDE;
            case TETORON_COTTON -> FabricTypeDTO.TETORON_COTTON;
            case TRIVINYL -> FabricTypeDTO.TRIVINYL;
            case VINYL -> FabricTypeDTO.VINYL;
            case HAIR -> FabricTypeDTO.HAIR;
            case COW_HAIR -> FabricTypeDTO.COW_HAIR;
            case HORSE_HAIR -> FabricTypeDTO.HORSE_HAIR;
            case GOAT_HAIR -> FabricTypeDTO.GOAT_HAIR;
            case SILK -> FabricTypeDTO.SILK;
            case ANGORA_WOOL -> FabricTypeDTO.ANGORA_WOOL;
            case BEAVER -> FabricTypeDTO.BEAVER;
            case CASHGORA_GOAT -> FabricTypeDTO.CASHGORA_GOAT;
            case CAMEL -> FabricTypeDTO.CAMEL;
            case LAMA -> FabricTypeDTO.LAMA;
            case ANGORA_GOAT -> FabricTypeDTO.ANGORA_GOAT;
            case WOOL -> FabricTypeDTO.WOOL;
            case ALPAKA -> FabricTypeDTO.ALPAKA;
            case OTTER -> FabricTypeDTO.OTTER;
            case VIRGIN_WOOL -> FabricTypeDTO.VIRGIN_WOOL;
            case YAK -> FabricTypeDTO.YAK;
            case UNKNOWN -> FabricTypeDTO.UNKNOWN;
        };
    }

}
