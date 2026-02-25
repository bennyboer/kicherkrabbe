package de.bennyboer.kicherkrabbe.offers.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.offers.*;
import de.bennyboer.kicherkrabbe.offers.persistence.lookup.LookupOffer;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MongoLookupOfferSerializer implements ReadModelSerializer<LookupOffer, MongoLookupOffer> {

    @Override
    public MongoLookupOffer serialize(LookupOffer readModel) {
        var result = new MongoLookupOffer();

        result.id = readModel.getId().getValue();
        result.version = readModel.getVersion().getValue();
        result.title = readModel.getTitle().getValue();
        result.size = readModel.getSize().getValue();
        result.categoryIds = readModel.getCategories().stream().map(OfferCategoryId::getValue).collect(Collectors.toSet());
        var mongoProduct = new MongoProduct();
        mongoProduct.id = readModel.getProduct().getId().getValue();
        mongoProduct.number = readModel.getProduct().getNumber().getValue();
        result.product = mongoProduct;
        result.imageIds = readModel.getImages().stream().map(ImageId::getValue).toList();
        result.links = readModel.getLinks().getLinks().stream()
                .map(this::toMongoLink)
                .collect(Collectors.toSet());
        result.fabricCompositionItems = readModel.getFabricComposition().getItems().stream()
                .map(this::toMongoFabricCompositionItem)
                .collect(Collectors.toSet());
        result.pricing = toMongoPricing(readModel.getPricing());
        result.notes = toMongoNotes(readModel.getNotes());
        result.published = readModel.isPublished();
        result.reserved = readModel.isReserved();
        result.createdAt = readModel.getCreatedAt();
        result.archivedAt = readModel.getArchivedAt().orElse(null);

        return result;
    }

    @Override
    public LookupOffer deserialize(MongoLookupOffer serialized) {
        var id = OfferId.of(serialized.id);
        var version = Version.of(serialized.version);
        var title = OfferTitle.of(serialized.title);
        var size = OfferSize.of(serialized.size);
        var categories = serialized.categoryIds != null
                ? serialized.categoryIds.stream().map(OfferCategoryId::of).collect(Collectors.toSet())
                : Set.<OfferCategoryId>of();
        var product = Product.of(ProductId.of(serialized.product.id), ProductNumber.of(serialized.product.number));
        var images = serialized.imageIds.stream().map(ImageId::of).toList();
        var links = Links.of(serialized.links.stream()
                .map(this::toLink)
                .collect(Collectors.toSet()));
        var fabricComposition = FabricComposition.of(serialized.fabricCompositionItems.stream()
                .map(this::toFabricCompositionItem)
                .collect(Collectors.toSet()));
        var pricing = toPricing(serialized.pricing);
        var notes = toNotes(serialized.notes);

        return LookupOffer.of(
                id,
                version,
                title,
                size,
                categories,
                product,
                images,
                links,
                fabricComposition,
                pricing,
                notes,
                serialized.published,
                serialized.reserved,
                serialized.createdAt,
                serialized.archivedAt
        );
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

    private MongoPricing toMongoPricing(Pricing pricing) {
        var result = new MongoPricing();
        result.priceAmount = pricing.getPrice().getAmount();
        result.priceCurrency = pricing.getPrice().getCurrency().getShortForm();
        pricing.getDiscountedPrice().ifPresent(dp -> {
            result.discountedPriceAmount = dp.getAmount();
            result.discountedPriceCurrency = dp.getCurrency().getShortForm();
        });
        result.effectivePriceAmount = pricing.getDiscountedPrice()
                .map(Money::getAmount)
                .orElse(pricing.getPrice().getAmount());
        result.priceHistory = pricing.getPriceHistory().stream()
                .map(this::toMongoPriceHistoryEntry)
                .toList();
        return result;
    }

    private Pricing toPricing(MongoPricing mongoPricing) {
        var price = Money.of(mongoPricing.priceAmount, Currency.fromShortForm(mongoPricing.priceCurrency));
        Money discountedPrice = null;
        if (mongoPricing.discountedPriceAmount != null) {
            discountedPrice = Money.of(mongoPricing.discountedPriceAmount, Currency.fromShortForm(mongoPricing.discountedPriceCurrency));
        }
        var priceHistory = mongoPricing.priceHistory != null
                ? mongoPricing.priceHistory.stream().map(this::toPriceHistoryEntry).toList()
                : List.<PriceHistoryEntry>of();
        return Pricing.of(price, discountedPrice, priceHistory);
    }

    private MongoPriceHistoryEntry toMongoPriceHistoryEntry(PriceHistoryEntry entry) {
        var result = new MongoPriceHistoryEntry();
        result.amount = entry.getPrice().getAmount();
        result.currency = entry.getPrice().getCurrency().getShortForm();
        result.timestamp = entry.getTimestamp();
        return result;
    }

    private PriceHistoryEntry toPriceHistoryEntry(MongoPriceHistoryEntry mongoEntry) {
        return PriceHistoryEntry.of(
                Money.of(mongoEntry.amount, Currency.fromShortForm(mongoEntry.currency)),
                mongoEntry.timestamp
        );
    }

    private MongoNotes toMongoNotes(Notes notes) {
        var result = new MongoNotes();
        result.description = notes.getDescription().getValue();
        result.contains = notes.getContains().map(Note::getValue).orElse(null);
        result.care = notes.getCare().map(Note::getValue).orElse(null);
        result.safety = notes.getSafety().map(Note::getValue).orElse(null);
        return result;
    }

    private Notes toNotes(MongoNotes mongoNotes) {
        return Notes.of(
                Note.of(mongoNotes.description),
                mongoNotes.contains != null ? Note.of(mongoNotes.contains) : null,
                mongoNotes.care != null ? Note.of(mongoNotes.care) : null,
                mongoNotes.safety != null ? Note.of(mongoNotes.safety) : null
        );
    }

}
