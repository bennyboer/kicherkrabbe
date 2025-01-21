package de.bennyboer.kicherkrabbe.products.persistence.lookup.product.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.products.persistence.lookup.product.LookupProduct;
import de.bennyboer.kicherkrabbe.products.product.*;

import java.util.stream.Collectors;

public class MongoLookupProductSerializer implements ReadModelSerializer<LookupProduct, MongoLookupProduct> {

    @Override
    public MongoLookupProduct serialize(LookupProduct product) {
        var result = new MongoLookupProduct();

        result.id = product.getId().getValue();
        result.version = product.getVersion().getValue();
        result.number = product.getNumber().getValue();
        result.images = product.getImages()
                .stream()
                .map(ImageId::getValue)
                .toList();
        result.links = product.getLinks()
                .getLinks()
                .stream()
                .map(this::toMongoLink)
                .toList();
        result.fabricComposition = toMongoFabricComposition(product.getFabricComposition());
        result.notes = toMongoNotes(product.getNotes());
        result.producedAt = product.getProducedAt();
        result.createdAt = product.getCreatedAt();

        return result;
    }

    @Override
    public LookupProduct deserialize(MongoLookupProduct product) {
        var id = ProductId.of(product.id);
        var version = Version.of(product.version);
        var number = ProductNumber.of(product.number);
        var images = product.images
                .stream()
                .map(ImageId::of)
                .toList();
        var links = Links.of(product.links
                .stream()
                .map(this::toInternalLink)
                .collect(Collectors.toSet()));
        var fabricComposition = toInternalFabricComposition(product.fabricComposition);
        var notes = toInternalNotes(product.notes);
        var producedAt = product.producedAt;
        var createdAt = product.createdAt;

        return LookupProduct.of(
                id,
                version,
                number,
                images,
                links,
                fabricComposition,
                notes,
                producedAt,
                createdAt
        );
    }

    private MongoFabricComposition toMongoFabricComposition(FabricComposition fabricComposition) {
        var result = new MongoFabricComposition();

        result.items = fabricComposition.getItems()
                .stream()
                .map(this::toMongoFabricCompositionItem)
                .toList();

        return result;
    }

    private MongoFabricCompositionItem toMongoFabricCompositionItem(FabricCompositionItem item) {
        var result = new MongoFabricCompositionItem();

        result.fabricType = toMongoFabricType(item.getFabricType());
        result.percentage = item.getPercentage().getValue();

        return result;
    }

    private FabricComposition toInternalFabricComposition(MongoFabricComposition fabricComposition) {
        var items = fabricComposition.items
                .stream()
                .map(this::toInternalFabricCompositionItem)
                .collect(Collectors.toSet());

        return FabricComposition.of(items);
    }

    private FabricCompositionItem toInternalFabricCompositionItem(MongoFabricCompositionItem item) {
        var fabricType = toInternalFabricType(item.fabricType);
        var percentage = LowPrecisionFloat.of(item.percentage);

        return FabricCompositionItem.of(fabricType, percentage);
    }

    private MongoNotes toMongoNotes(Notes notes) {
        var result = new MongoNotes();

        result.contains = notes.getContains().getValue();
        result.care = notes.getCare().getValue();
        result.safety = notes.getSafety().getValue();

        return result;
    }

    private Notes toInternalNotes(MongoNotes notes) {
        var contains = Note.of(notes.contains);
        var care = Note.of(notes.care);
        var safety = Note.of(notes.safety);

        return Notes.of(contains, care, safety);
    }

    private MongoLink toMongoLink(Link link) {
        var result = new MongoLink();

        result.type = toMongoLinkType(link.getType());
        result.id = link.getId().getValue();
        result.name = link.getName().getValue();

        return result;
    }

    private Link toInternalLink(MongoLink link) {
        var type = toInternalLinkType(link.type);
        var id = LinkId.of(link.id);
        var name = LinkName.of(link.name);

        return Link.of(type, id, name);
    }

    private MongoLinkType toMongoLinkType(LinkType type) {
        return switch (type) {
            case PATTERN -> MongoLinkType.PATTERN;
            case FABRIC -> MongoLinkType.FABRIC;
        };
    }

    private LinkType toInternalLinkType(MongoLinkType type) {
        return switch (type) {
            case PATTERN -> LinkType.PATTERN;
            case FABRIC -> LinkType.FABRIC;
        };
    }

    private MongoFabricType toMongoFabricType(FabricType type) {
        return switch (type) {
            case ABACA -> MongoFabricType.ABACA;
            case ALFA -> MongoFabricType.ALFA;
            case BAMBOO -> MongoFabricType.BAMBOO;
            case HEMP -> MongoFabricType.HEMP;
            case COTTON -> MongoFabricType.COTTON;
            case COCONUT -> MongoFabricType.COCONUT;
            case CASHMERE -> MongoFabricType.CASHMERE;
            case HENEQUEN -> MongoFabricType.HENEQUEN;
            case HALF_LINEN -> MongoFabricType.HALF_LINEN;
            case JUTE -> MongoFabricType.JUTE;
            case KENAF -> MongoFabricType.KENAF;
            case KAPOK -> MongoFabricType.KAPOK;
            case LINEN -> MongoFabricType.LINEN;
            case MAGUEY -> MongoFabricType.MAGUEY;
            case RAMIE -> MongoFabricType.RAMIE;
            case SISAL -> MongoFabricType.SISAL;
            case SUNN -> MongoFabricType.SUNN;
            case CELLULOSE_ACETATE -> MongoFabricType.CELLULOSE_ACETATE;
            case CUPRO -> MongoFabricType.CUPRO;
            case LYOCELL -> MongoFabricType.LYOCELL;
            case MODAL -> MongoFabricType.MODAL;
            case PAPER -> MongoFabricType.PAPER;
            case TRIACETATE -> MongoFabricType.TRIACETATE;
            case VISCOSE -> MongoFabricType.VISCOSE;
            case ARAMID -> MongoFabricType.ARAMID;
            case CARBON_FIBER -> MongoFabricType.CARBON_FIBER;
            case CHLORO_FIBER -> MongoFabricType.CHLORO_FIBER;
            case ELASTANE -> MongoFabricType.ELASTANE;
            case FLUOR_FIBER -> MongoFabricType.FLUOR_FIBER;
            case LUREX -> MongoFabricType.LUREX;
            case MODACRYLIC -> MongoFabricType.MODACRYLIC;
            case NYLON -> MongoFabricType.NYLON;
            case POLYAMIDE -> MongoFabricType.POLYAMIDE;
            case POLYCARBAMIDE -> MongoFabricType.POLYCARBAMIDE;
            case ACRYLIC -> MongoFabricType.ACRYLIC;
            case POLYETHYLENE -> MongoFabricType.POLYETHYLENE;
            case POLYESTER -> MongoFabricType.POLYESTER;
            case POLYPROPYLENE -> MongoFabricType.POLYPROPYLENE;
            case POLYURETHANE -> MongoFabricType.POLYURETHANE;
            case POLYVINYL_CHLORIDE -> MongoFabricType.POLYVINYL_CHLORIDE;
            case TETORON_COTTON -> MongoFabricType.TETORON_COTTON;
            case TRIVINYL -> MongoFabricType.TRIVINYL;
            case VINYL -> MongoFabricType.VINYL;
            case HAIR -> MongoFabricType.HAIR;
            case COW_HAIR -> MongoFabricType.COW_HAIR;
            case HORSE_HAIR -> MongoFabricType.HORSE_HAIR;
            case GOAT_HAIR -> MongoFabricType.GOAT_HAIR;
            case SILK -> MongoFabricType.SILK;
            case ANGORA_WOOL -> MongoFabricType.ANGORA_WOOL;
            case BEAVER -> MongoFabricType.BEAVER;
            case CASHGORA_GOAT -> MongoFabricType.CASHGORA_GOAT;
            case CAMEL -> MongoFabricType.CAMEL;
            case LAMA -> MongoFabricType.LAMA;
            case ANGORA_GOAT -> MongoFabricType.ANGORA_GOAT;
            case WOOL -> MongoFabricType.WOOL;
            case ALPAKA -> MongoFabricType.ALPAKA;
            case OTTER -> MongoFabricType.OTTER;
            case VIRGIN_WOOL -> MongoFabricType.VIRGIN_WOOL;
            case YAK -> MongoFabricType.YAK;
            case UNKNOWN -> MongoFabricType.UNKNOWN;
        };
    }

    private FabricType toInternalFabricType(MongoFabricType type) {
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

}
