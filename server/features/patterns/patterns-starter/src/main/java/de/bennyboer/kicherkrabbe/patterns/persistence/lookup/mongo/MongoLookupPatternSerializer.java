package de.bennyboer.kicherkrabbe.patterns.persistence.lookup.mongo;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.readmodel.mongo.ReadModelSerializer;
import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.*;
import de.bennyboer.kicherkrabbe.patterns.persistence.lookup.LookupPattern;

import java.util.Optional;
import java.util.stream.Collectors;

public class MongoLookupPatternSerializer implements ReadModelSerializer<LookupPattern, MongoLookupPattern> {

    @Override
    public MongoLookupPattern serialize(LookupPattern readModel) {
        var result = new MongoLookupPattern();

        result.id = readModel.getId().getValue();
        result.version = readModel.getVersion().getValue();
        result.published = readModel.isPublished();
        result.name = readModel.getName().getValue();
        result.number = readModel.getNumber().getValue();
        result.description = readModel.getDescription()
                .map(PatternDescription::getValue)
                .orElse(null);
        result.alias = readModel.getAlias().getValue();
        result.attribution = new MongoLookupPatternAttribution();
        result.attribution.originalPatternName = readModel.getAttribution()
                .getOriginalPatternName()
                .map(OriginalPatternName::getValue)
                .orElse(null);
        result.attribution.designer = readModel.getAttribution()
                .getDesigner()
                .map(PatternDesigner::getValue)
                .orElse(null);
        result.categories = readModel.getCategories()
                .stream()
                .map(PatternCategoryId::getValue)
                .collect(Collectors.toSet());
        result.images = readModel.getImages()
                .stream()
                .map(ImageId::getValue)
                .toList();
        result.variants = readModel.getVariants()
                .stream()
                .map(variant -> {
                    var mongoVariant = new MongoLookupPatternVariant();

                    mongoVariant.name = variant.getName().getValue();
                    mongoVariant.pricedSizeRanges = variant.getPricedSizeRanges()
                            .stream()
                            .map(pricedSizeRange -> {
                                var mongoPricedSizeRange = new MongoLookupPricedSizeRange();

                                mongoPricedSizeRange.from = pricedSizeRange.getFrom();
                                mongoPricedSizeRange.to = pricedSizeRange.getTo().orElse(null);
                                mongoPricedSizeRange.unit = pricedSizeRange.getUnit().orElse(null);
                                mongoPricedSizeRange.price = new MongoLookupPrice();
                                mongoPricedSizeRange.price.amount = pricedSizeRange.getPrice().getAmount();
                                mongoPricedSizeRange.price.currency = pricedSizeRange.getPrice()
                                        .getCurrency()
                                        .getShortForm();

                                return mongoPricedSizeRange;
                            })
                            .collect(Collectors.toSet());

                    return mongoVariant;
                })
                .toList();
        result.extras = readModel.getExtras()
                .stream()
                .map(extra -> {
                    var mongoExtra = new MongoLookupPatternExtra();

                    mongoExtra.name = extra.getName().getValue();
                    mongoExtra.price = new MongoLookupPrice();
                    mongoExtra.price.amount = extra.getPrice().getAmount();
                    mongoExtra.price.currency = extra.getPrice().getCurrency().getShortForm();

                    return mongoExtra;
                })
                .toList();
        result.createdAt = readModel.getCreatedAt();

        return result;
    }

    @Override
    public LookupPattern deserialize(MongoLookupPattern serialized) {
        var id = PatternId.of(serialized.id);
        var version = Version.of(serialized.version);
        var published = serialized.published;
        var name = PatternName.of(serialized.name);
        var number = PatternNumber.of(serialized.number);
        var description = Optional.ofNullable(serialized.description)
                .map(PatternDescription::of)
                .orElse(null);
        var alias = PatternAlias.of(serialized.alias);
        var attribution = PatternAttribution.of(
                Optional.ofNullable(serialized.attribution.originalPatternName)
                        .map(OriginalPatternName::of)
                        .orElse(null),
                Optional.ofNullable(serialized.attribution.designer).map(PatternDesigner::of).orElse(null)
        );
        var categories = serialized.categories
                .stream()
                .map(PatternCategoryId::of)
                .collect(Collectors.toSet());
        var images = serialized.images
                .stream()
                .map(ImageId::of)
                .toList();
        var variants = serialized.variants
                .stream()
                .map(variant -> {
                    var variantName = PatternVariantName.of(variant.name);
                    var pricedSizeRanges = variant.pricedSizeRanges
                            .stream()
                            .map(pricedSizeRange -> {
                                var from = pricedSizeRange.from;
                                var to = pricedSizeRange.to;
                                var unit = pricedSizeRange.unit;
                                var price = Money.of(
                                        pricedSizeRange.price.amount,
                                        Currency.fromShortForm(pricedSizeRange.price.currency)
                                );
                                return PricedSizeRange.of(from, to, unit, price);
                            })
                            .collect(Collectors.toSet());

                    return PatternVariant.of(variantName, pricedSizeRanges);
                })
                .toList();
        var extras = serialized.extras
                .stream()
                .map(extra -> {
                    var extraName = PatternExtraName.of(extra.name);
                    var price = Money.of(extra.price.amount, Currency.fromShortForm(extra.price.currency));
                    return PatternExtra.of(extraName, price);
                })
                .toList();
        var createdAt = serialized.createdAt;

        return LookupPattern.of(
                id,
                version,
                published,
                name,
                number,
                description,
                alias,
                attribution,
                categories,
                images,
                variants,
                extras,
                createdAt
        );
    }

}
