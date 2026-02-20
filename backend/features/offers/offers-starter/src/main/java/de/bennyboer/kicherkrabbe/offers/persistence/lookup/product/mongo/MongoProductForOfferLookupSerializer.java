package de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.offers.*;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.mongo.MongoFabricCompositionItem;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.mongo.MongoLink;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.product.LookupProduct;

import java.util.stream.Collectors;

public class MongoProductForOfferLookupSerializer
        implements ReadModelSerializer<LookupProduct, MongoProductForOfferLookup> {

    @Override
    public MongoProductForOfferLookup serialize(LookupProduct readModel) {
        var result = new MongoProductForOfferLookup();

        result.id = readModel.getId().getValue();
        result.version = readModel.getVersion().getValue();
        result.number = readModel.getNumber().getValue();
        result.links = readModel.getLinks().getLinks().stream()
                .map(this::toMongoLink)
                .collect(Collectors.toSet());
        result.fabricCompositionItems = readModel.getFabricComposition().getItems().stream()
                .map(this::toMongoFabricCompositionItem)
                .collect(Collectors.toSet());

        return result;
    }

    @Override
    public LookupProduct deserialize(MongoProductForOfferLookup serialized) {
        var id = ProductId.of(serialized.id);
        var version = Version.of(serialized.version);
        var number = ProductNumber.of(serialized.number);
        var links = Links.of(serialized.links.stream()
                .map(this::toLink)
                .collect(Collectors.toSet()));
        var fabricComposition = FabricComposition.of(serialized.fabricCompositionItems.stream()
                .map(this::toFabricCompositionItem)
                .collect(Collectors.toSet()));

        return LookupProduct.of(id, version, number, links, fabricComposition);
    }

    private MongoLink toMongoLink(Link link) {
        var result = new MongoLink();
        result.type = link.getType().name();
        result.id = link.getId().getValue();
        result.name = link.getName().getValue();
        return result;
    }

    private Link toLink(MongoLink mongoLink) {
        return Link.of(
                LinkType.valueOf(mongoLink.type),
                LinkId.of(mongoLink.id),
                LinkName.of(mongoLink.name)
        );
    }

    private MongoFabricCompositionItem toMongoFabricCompositionItem(FabricCompositionItem item) {
        var result = new MongoFabricCompositionItem();
        result.fabricType = item.getFabricType().name();
        result.percentage = item.getPercentage().getValue();
        return result;
    }

    private FabricCompositionItem toFabricCompositionItem(MongoFabricCompositionItem mongoItem) {
        return FabricCompositionItem.of(
                FabricType.valueOf(mongoItem.fabricType),
                LowPrecisionFloat.of(mongoItem.percentage)
        );
    }

}
