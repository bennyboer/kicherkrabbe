package de.bennyboer.kicherkrabbe.patterns.persistence;

import de.bennyboer.kicherkrabbe.eventsourcing.EventSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.EventName;
import de.bennyboer.kicherkrabbe.money.Currency;
import de.bennyboer.kicherkrabbe.money.Money;
import de.bennyboer.kicherkrabbe.patterns.*;
import de.bennyboer.kicherkrabbe.patterns.create.CreatedEvent;
import de.bennyboer.kicherkrabbe.patterns.delete.DeletedEvent;
import de.bennyboer.kicherkrabbe.patterns.publish.PublishedEvent;
import de.bennyboer.kicherkrabbe.patterns.rename.RenamedEvent;
import de.bennyboer.kicherkrabbe.patterns.snapshot.SnapshottedEvent;
import de.bennyboer.kicherkrabbe.patterns.unpublish.UnpublishedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.attribution.AttributionUpdatedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.categories.CategoriesUpdatedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.extras.ExtrasUpdatedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.images.ImagesUpdatedEvent;
import de.bennyboer.kicherkrabbe.patterns.update.variants.VariantsUpdatedEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PatternEventPayloadSerializer implements EventSerializer {

    @Override
    public Map<String, Object> serialize(Event event) {
        return switch (event) {
            case CreatedEvent e -> Map.of(
                    "name", e.getName().getValue(),
                    "attribution", serializeAttribution(e.getAttribution()),
                    "categories", serializeCategories(e.getCategories()),
                    "images", serializeImages(e.getImages()),
                    "variants", serializeVariants(e.getVariants()),
                    "extras", serializeExtras(e.getExtras())
            );
            case SnapshottedEvent e -> {
                Map<String, Object> result = new HashMap<>(Map.of(
                        "published", e.isPublished(),
                        "name", e.getName().getValue(),
                        "attribution", serializeAttribution(e.getAttribution()),
                        "categories", serializeCategories(e.getCategories()),
                        "images", serializeImages(e.getImages()),
                        "variants", serializeVariants(e.getVariants()),
                        "extras", serializeExtras(e.getExtras()),
                        "createdAt", e.getCreatedAt().toString()
                ));

                e.getDeletedAt().ifPresent(deletedAt -> result.put("deletedAt", deletedAt.toString()));

                yield result;
            }
            case PublishedEvent ignored -> Map.of();
            case UnpublishedEvent ignored -> Map.of();
            case RenamedEvent e -> Map.of(
                    "name", e.getName().getValue()
            );
            case AttributionUpdatedEvent e -> Map.of(
                    "attribution", serializeAttribution(e.getAttribution())
            );
            case CategoriesUpdatedEvent e -> Map.of(
                    "categories", serializeCategories(e.getCategories())
            );
            case ImagesUpdatedEvent e -> Map.of(
                    "images", serializeImages(e.getImages())
            );
            case VariantsUpdatedEvent e -> Map.of(
                    "variants", serializeVariants(e.getVariants())
            );
            case ExtrasUpdatedEvent e -> Map.of(
                    "extras", serializeExtras(e.getExtras())
            );
            case DeletedEvent ignored -> Map.of();
            default -> throw new IllegalStateException("Unexpected event: " + event);
        };
    }

    @Override
    public Event deserialize(EventName name, Version eventVersion, Map<String, Object> payload) {
        return switch (name.getValue()) {
            case "CREATED" -> CreatedEvent.of(
                    PatternName.of((String) payload.get("name")),
                    deserializeAttribution((Map<String, Object>) payload.get("attribution")),
                    deserializeCategories((Set<String>) payload.get("categories")),
                    deserializeImages((List<String>) payload.get("images")),
                    deserializeVariants((List<Map<String, Object>>) payload.get("variants")),
                    deserializeExtras((List<Map<String, Object>>) payload.get("extras"))
            );
            case "SNAPSHOTTED" -> SnapshottedEvent.of(
                    (boolean) payload.get("published"),
                    PatternName.of((String) payload.get("name")),
                    deserializeAttribution((Map<String, Object>) payload.get("attribution")),
                    deserializeCategories((Set<String>) payload.get("categories")),
                    deserializeImages((List<String>) payload.get("images")),
                    deserializeVariants((List<Map<String, Object>>) payload.get("variants")),
                    deserializeExtras((List<Map<String, Object>>) payload.get("extras")),
                    Instant.parse((String) payload.get("createdAt")),
                    payload.containsKey("deletedAt") ?
                            Instant.parse((String) payload.get("deletedAt")) :
                            null
            );
            case "PUBLISHED" -> PublishedEvent.of();
            case "UNPUBLISHED" -> UnpublishedEvent.of();
            case "RENAMED" -> RenamedEvent.of(PatternName.of((String) payload.get("name")));
            case "ATTRIBUTION_UPDATED" ->
                    AttributionUpdatedEvent.of(deserializeAttribution((Map<String, Object>) payload.get("attribution")));
            case "CATEGORIES_UPDATED" -> CategoriesUpdatedEvent.of(deserializeCategories((Set<String>) payload.get(
                    "categories")));
            case "IMAGES_UPDATED" -> ImagesUpdatedEvent.of(deserializeImages((List<String>) payload.get("images")));
            case "VARIANTS_UPDATED" ->
                    VariantsUpdatedEvent.of(deserializeVariants((List<Map<String, Object>>) payload.get("variants")));
            case "EXTRAS_UPDATED" -> ExtrasUpdatedEvent.of(deserializeExtras((List<Map<String, Object>>) payload.get(
                    "extras")));
            case "DELETED" -> DeletedEvent.of();
            default -> throw new IllegalStateException("Unexpected event name: " + name);
        };
    }

    private Map<String, Object> serializeAttribution(PatternAttribution attribution) {
        Map<String, Object> result = new HashMap<>();

        attribution.getOriginalPatternName().ifPresent(originalPatternName -> result.put(
                "originalPatternName",
                originalPatternName.getValue()
        ));
        attribution.getDesigner().ifPresent(designer -> result.put(
                "designer",
                designer.getValue()
        ));

        return result;
    }

    private PatternAttribution deserializeAttribution(Map<String, Object> payload) {
        OriginalPatternName originalPatternName = payload.containsKey("originalPatternName") ?
                OriginalPatternName.of((String) payload.get("originalPatternName")) :
                null;
        PatternDesigner designer = payload.containsKey("designer") ?
                PatternDesigner.of((String) payload.get("designer")) :
                null;

        return PatternAttribution.of(originalPatternName, designer);
    }

    private Set<String> serializeCategories(Set<PatternCategoryId> categories) {
        return categories.stream().map(PatternCategoryId::getValue).collect(Collectors.toSet());
    }

    private List<String> serializeImages(List<ImageId> images) {
        return images.stream().map(ImageId::getValue).toList();
    }

    private Set<PatternCategoryId> deserializeCategories(Set<String> payload) {
        return payload.stream().map(PatternCategoryId::of).collect(Collectors.toSet());
    }

    private List<ImageId> deserializeImages(List<String> payload) {
        return payload.stream().map(ImageId::of).toList();
    }

    private List<Map<String, Object>> serializeVariants(List<PatternVariant> variants) {
        return variants.stream().map(this::serializeVariant).toList();
    }

    private List<PatternVariant> deserializeVariants(List<Map<String, Object>> variants) {
        return variants.stream().map(this::deserializeVariant).toList();
    }

    private Map<String, Object> serializeVariant(PatternVariant variant) {
        return Map.of(
                "name", variant.getName().getValue(),
                "pricedSizeRanges", serializePricedSizeRanges(variant.getPricedSizeRanges())
        );
    }

    private PatternVariant deserializeVariant(Map<String, Object> variant) {
        PatternVariantName name = PatternVariantName.of((String) variant.get("name"));
        Set<PricedSizeRange> pricedSizeRanges = deserializePricedSizeRanges(
                (List<Map<String, Object>>) variant.get("pricedSizeRanges")
        );

        return PatternVariant.of(name, pricedSizeRanges);
    }

    private List<Map<String, Object>> serializeExtras(List<PatternExtra> extras) {
        return extras.stream().map(this::serializeExtra).toList();
    }

    private List<PatternExtra> deserializeExtras(List<Map<String, Object>> extras) {
        return extras.stream().map(this::deserializeExtra).toList();
    }

    private Map<String, Object> serializeExtra(PatternExtra extra) {
        return Map.of(
                "name", extra.getName().getValue(),
                "price", serializeMoney(extra.getPrice())
        );
    }

    private PatternExtra deserializeExtra(Map<String, Object> extra) {
        PatternExtraName name = PatternExtraName.of((String) extra.get("name"));
        Money price = deserializeMoney((Map<String, Object>) extra.get("price"));

        return PatternExtra.of(name, price);
    }

    private Set<Map<String, Object>> serializePricedSizeRanges(Set<PricedSizeRange> pricedSizeRanges) {
        return pricedSizeRanges.stream().map(this::serializePricedSizeRange).collect(Collectors.toSet());
    }

    private Set<PricedSizeRange> deserializePricedSizeRanges(List<Map<String, Object>> pricedSizeRanges) {
        return pricedSizeRanges.stream().map(this::deserializePricedSizeRange).collect(Collectors.toSet());
    }

    private Map<String, Object> serializePricedSizeRange(PricedSizeRange pricedSizeRange) {
        Map<String, Object> result = new HashMap<>(Map.of(
                "from", pricedSizeRange.getFrom(),
                "price", serializeMoney(pricedSizeRange.getPrice())
        ));

        pricedSizeRange.getTo().ifPresent(to -> result.put("to", to));
        pricedSizeRange.getUnit().ifPresent(unit -> result.put("unit", unit));

        return result;
    }

    private PricedSizeRange deserializePricedSizeRange(Map<String, Object> pricedSizeRange) {
        long from = (long) pricedSizeRange.get("from");
        Long to = pricedSizeRange.containsKey("to") ? (long) pricedSizeRange.get("to") : null;
        String unit = pricedSizeRange.containsKey("unit") ? (String) pricedSizeRange.get("unit") : null;
        Money price = deserializeMoney((Map<String, Object>) pricedSizeRange.get("price"));

        return PricedSizeRange.of(from, to, unit, price);
    }

    private Map<String, Object> serializeMoney(Money money) {
        return Map.of(
                "amount", money.getAmount(),
                "currency", money.getCurrency().getShortForm()
        );
    }

    private Money deserializeMoney(Map<String, Object> money) {
        long amount = (long) money.get("amount");
        Currency currency = Currency.fromShortForm((String) money.get("currency"));

        return Money.of(amount, currency);
    }

}
