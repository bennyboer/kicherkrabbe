package de.bennyboer.kicherkrabbe.eventsourcing.example.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.AggregateSnapshotDeserializer;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotExclude;
import jakarta.annotation.Nullable;
import lombok.Value;
import lombok.With;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;

public class AggregateSnapshotDeserializerTest {

    private final AggregateSnapshotDeserializer deserializer = new AggregateSnapshotDeserializer();

    @Test
    void shouldDeserializePrimitiveTypes() {
        Map<String, Object> state = new HashMap<>();
        state.put("stringValue", "test string");
        state.put("intValue", 42);
        state.put("longValue", 123456789L);
        state.put("doubleValue", 3.14);
        state.put("floatValue", 2.5f);
        state.put("booleanValue", true);

        var snapshot = SnapshotEvent.of(state);
        var metadata = createMetadata("test-id", 5);

        var aggregate = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(aggregate.getStringValue()).isEqualTo("test string");
        assertThat(aggregate.getIntValue()).isEqualTo(42);
        assertThat(aggregate.getLongValue()).isEqualTo(123456789L);
        assertThat(aggregate.getDoubleValue()).isEqualTo(3.14);
        assertThat(aggregate.getFloatValue()).isEqualTo(2.5f);
        assertThat(aggregate.isBooleanValue()).isTrue();
    }

    @Test
    void shouldDeserializeInstant() {
        Map<String, Object> state = new HashMap<>();
        state.put("instantValue", "2024-01-15T10:30:00Z");

        var snapshot = SnapshotEvent.of(state);
        var metadata = createMetadata("test-id", 0);

        var aggregate = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(aggregate.getInstantValue()).isEqualTo(Instant.parse("2024-01-15T10:30:00Z"));
    }

    @Test
    void shouldDeserializeDuration() {
        Map<String, Object> state = new HashMap<>();
        state.put("durationValue", "PT2H30M");

        var snapshot = SnapshotEvent.of(state);
        var metadata = createMetadata("test-id", 0);

        var aggregate = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(aggregate.getDurationValue()).isEqualTo(Duration.ofHours(2).plusMinutes(30));
    }

    @Test
    void shouldDeserializeEnum() {
        Map<String, Object> state = new HashMap<>();
        state.put("enumValue", "ACTIVE");

        var snapshot = SnapshotEvent.of(state);
        var metadata = createMetadata("test-id", 0);

        var aggregate = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(aggregate.getEnumValue()).isEqualTo(TestStatus.ACTIVE);
    }

    @Test
    void shouldDeserializeList() {
        Map<String, Object> state = new HashMap<>();
        state.put("listValue", List.of("item1", "item2", "item3"));

        var snapshot = SnapshotEvent.of(state);
        var metadata = createMetadata("test-id", 0);

        var aggregate = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(aggregate.getListValue()).containsExactly("item1", "item2", "item3");
    }

    @Test
    void shouldDeserializeSet() {
        Map<String, Object> state = new HashMap<>();
        state.put("setValue", List.of("a", "b", "c"));

        var snapshot = SnapshotEvent.of(state);
        var metadata = createMetadata("test-id", 0);

        var aggregate = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(aggregate.getSetValue()).containsExactlyInAnyOrder("a", "b", "c");
    }

    @Test
    void shouldDeserializeMap() {
        Map<String, Object> state = new HashMap<>();
        state.put("mapValue", Map.of("key1", "value1", "key2", "value2"));

        var snapshot = SnapshotEvent.of(state);
        var metadata = createMetadata("test-id", 0);

        var aggregate = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(aggregate.getMapValue())
                .containsEntry("key1", "value1")
                .containsEntry("key2", "value2");
    }

    @Test
    void shouldDeserializeValueObject() {
        Map<String, Object> state = new HashMap<>();
        state.put("valueObject", "inner value");

        var snapshot = SnapshotEvent.of(state);
        var metadata = createMetadata("test-id", 0);

        var aggregate = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(aggregate.getValueObject()).isNotNull();
        assertThat(aggregate.getValueObject().getValue()).isEqualTo("inner value");
    }

    @Test
    void shouldDeserializeNestedObject() {
        Map<String, Object> state = new HashMap<>();
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("name", "nested name");
        nestedMap.put("count", 99);
        state.put("nestedObject", nestedMap);

        var snapshot = SnapshotEvent.of(state);
        var metadata = createMetadata("test-id", 0);

        var aggregate = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(aggregate.getNestedObject()).isNotNull();
        assertThat(aggregate.getNestedObject().getName()).isEqualTo("nested name");
        assertThat(aggregate.getNestedObject().getCount()).isEqualTo(99);
    }

    @Test
    void shouldDeserializeListOfValueObjects() {
        Map<String, Object> state = new HashMap<>();
        state.put("valueObjectList", List.of("first", "second"));

        var snapshot = SnapshotEvent.of(state);
        var metadata = createMetadata("test-id", 0);

        var aggregate = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(aggregate.getValueObjectList()).hasSize(2);
        assertThat(aggregate.getValueObjectList().get(0).getValue()).isEqualTo("first");
        assertThat(aggregate.getValueObjectList().get(1).getValue()).isEqualTo("second");
    }

    @Test
    void shouldSetIdFromMetadataForExcludedIdField() {
        Map<String, Object> state = new HashMap<>();
        state.put("stringValue", "test");

        var snapshot = SnapshotEvent.of(state);
        var metadata = createMetadata("restored-id", 0);

        var aggregate = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(aggregate.getId()).isEqualTo("restored-id");
    }

    @Test
    void shouldSetVersionFromMetadataForExcludedVersionField() {
        Map<String, Object> state = new HashMap<>();
        state.put("stringValue", "test");

        var snapshot = SnapshotEvent.of(state);
        var metadata = createMetadata("test-id", 42);

        var aggregate = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(aggregate.getVersion()).isEqualTo(Version.of(42));
    }

    @Test
    void shouldHandleNullValues() {
        Map<String, Object> state = new HashMap<>();
        state.put("stringValue", null);
        state.put("instantValue", null);
        state.put("durationValue", null);
        state.put("enumValue", null);

        var snapshot = SnapshotEvent.of(state);
        var metadata = createMetadata("test-id", 0);

        var aggregate = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(aggregate.getStringValue()).isNull();
        assertThat(aggregate.getInstantValue()).isNull();
        assertThat(aggregate.getDurationValue()).isNull();
        assertThat(aggregate.getEnumValue()).isNull();
    }

    @Test
    void shouldDeserializeNumbersFromDifferentFormats() {
        Map<String, Object> state = new HashMap<>();
        state.put("intValue", 42L);
        state.put("longValue", 123);
        state.put("doubleValue", 3);
        state.put("floatValue", 2.0);

        var snapshot = SnapshotEvent.of(state);
        var metadata = createMetadata("test-id", 0);

        var aggregate = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(aggregate.getIntValue()).isEqualTo(42);
        assertThat(aggregate.getLongValue()).isEqualTo(123L);
        assertThat(aggregate.getDoubleValue()).isEqualTo(3.0);
        assertThat(aggregate.getFloatValue()).isEqualTo(2.0f);
    }

    private EventMetadata createMetadata(String aggregateId, long version) {
        return EventMetadata.of(
                AggregateId.of(aggregateId),
                AggregateType.of("TEST"),
                Version.of(version),
                Agent.system(),
                Instant.now(),
                true
        );
    }

    @Value
    @With(PRIVATE)
    static class TestAggregate implements Aggregate {

        public static final AggregateType TYPE = AggregateType.of("TEST");

        @SnapshotExclude
        String id;

        @SnapshotExclude
        Version version;

        String stringValue;

        int intValue;

        long longValue;

        double doubleValue;

        float floatValue;

        boolean booleanValue;

        @Nullable
        Instant instantValue;

        @Nullable
        Duration durationValue;

        @Nullable
        TestStatus enumValue;

        @Nullable
        List<String> listValue;

        @Nullable
        Set<String> setValue;

        @Nullable
        Map<String, String> mapValue;

        @Nullable
        TestValueObject valueObject;

        @Nullable
        TestNestedObject nestedObject;

        @Nullable
        List<TestValueObject> valueObjectList;

        public static TestAggregate init() {
            return new TestAggregate(
                    null, null, null, 0, 0L, 0.0, 0.0f, false,
                    null, null, null, null, null, null, null, null, null
            );
        }

        @Override
        public ApplyCommandResult apply(Command cmd, Agent agent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Aggregate apply(Event event, EventMetadata metadata) {
            throw new UnsupportedOperationException();
        }

    }

    enum TestStatus {
        ACTIVE,
        INACTIVE,
        PENDING
    }

    @Value
    static class TestValueObject {

        String value;

        public static TestValueObject of(String value) {
            return new TestValueObject(value);
        }

        public String getValue() {
            return value;
        }

    }

    @Value
    static class TestNestedObject {

        String name;

        int count;

        public static TestNestedObject of(String name, int count) {
            return new TestNestedObject(name, count);
        }

    }

    @Value
    static class TestId {

        String value;

        public static TestId of(String value) {
            return new TestId(value);
        }

        public String getValue() {
            return value;
        }

    }

    @Value
    static class TestAvailability {

        TestId typeId;

        boolean inStock;

        public static TestAvailability of(TestId typeId, boolean inStock) {
            return new TestAvailability(typeId, inStock);
        }

    }

    @Value
    @With(PRIVATE)
    static class TypedIdAggregate implements Aggregate {

        public static final AggregateType TYPE = AggregateType.of("TYPED_ID_TEST");

        @SnapshotExclude
        TestId id;

        @SnapshotExclude
        Version version;

        TestId referenceId;

        Set<TestId> idSet;

        Set<TestAvailability> availabilities;

        public static TypedIdAggregate init() {
            return new TypedIdAggregate(null, null, null, null, null);
        }

        @Override
        public ApplyCommandResult apply(Command cmd, Agent agent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Aggregate apply(Event event, EventMetadata metadata) {
            throw new UnsupportedOperationException();
        }

    }

    @Test
    void shouldDeserializeTypedId() {
        Map<String, Object> state = new HashMap<>();
        state.put("referenceId", "reference-123");

        var snapshot = SnapshotEvent.of(state);
        var metadata = createTypedIdMetadata("aggregate-id", 5);

        var aggregate = deserializer.deserialize(snapshot, TypedIdAggregate.init(), metadata);

        assertThat(aggregate.getReferenceId()).isNotNull();
        assertThat(aggregate.getReferenceId().getValue()).isEqualTo("reference-123");
    }

    @Test
    void shouldDeserializeTypedIdFromMetadataForExcludedField() {
        Map<String, Object> state = new HashMap<>();
        state.put("referenceId", "some-ref");

        var snapshot = SnapshotEvent.of(state);
        var metadata = createTypedIdMetadata("restored-aggregate-id", 10);

        var aggregate = deserializer.deserialize(snapshot, TypedIdAggregate.init(), metadata);

        assertThat(aggregate.getId()).isNotNull();
        assertThat(aggregate.getId().getValue()).isEqualTo("restored-aggregate-id");
        assertThat(aggregate.getVersion()).isEqualTo(Version.of(10));
    }

    @Test
    void shouldDeserializeSetOfTypedIds() {
        Map<String, Object> state = new HashMap<>();
        state.put("idSet", List.of("id-1", "id-2", "id-3"));

        var snapshot = SnapshotEvent.of(state);
        var metadata = createTypedIdMetadata("aggregate-id", 5);

        var aggregate = deserializer.deserialize(snapshot, TypedIdAggregate.init(), metadata);

        assertThat(aggregate.getIdSet()).hasSize(3);
        var idValues = aggregate.getIdSet().stream()
                .map(TestId::getValue)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(idValues).containsExactlyInAnyOrder("id-1", "id-2", "id-3");
    }

    @Test
    void shouldDeserializeSetOfComplexNestedObjects() {
        Map<String, Object> state = new HashMap<>();
        state.put("availabilities", List.of(
                Map.of("typeId", "type-1", "inStock", true),
                Map.of("typeId", "type-2", "inStock", false)
        ));

        var snapshot = SnapshotEvent.of(state);
        var metadata = createTypedIdMetadata("aggregate-id", 5);

        var aggregate = deserializer.deserialize(snapshot, TypedIdAggregate.init(), metadata);

        assertThat(aggregate.getAvailabilities()).hasSize(2);

        var availabilityMap = aggregate.getAvailabilities().stream()
                .collect(java.util.stream.Collectors.toMap(
                        a -> a.getTypeId().getValue(),
                        TestAvailability::isInStock
                ));

        assertThat(availabilityMap).containsEntry("type-1", true);
        assertThat(availabilityMap).containsEntry("type-2", false);
    }

    private EventMetadata createTypedIdMetadata(String aggregateId, long version) {
        return EventMetadata.of(
                AggregateId.of(aggregateId),
                AggregateType.of("TYPED_ID_TEST"),
                Version.of(version),
                Agent.system(),
                Instant.now(),
                true
        );
    }

    @Test
    void shouldDeserializeValueObjectUsingConstructorWhenNoFactoryMethod() {
        Map<String, Object> state = new HashMap<>();
        state.put("constructorOnlyObject", "test-value");

        var snapshot = SnapshotEvent.of(state);
        var metadata = createConstructorTestMetadata("test-id", 1);

        var aggregate = deserializer.deserialize(snapshot, ConstructorTestAggregate.init(), metadata);

        assertThat(aggregate.getConstructorOnlyObject()).isNotNull();
        assertThat(aggregate.getConstructorOnlyObject().getValue()).isEqualTo("test-value");
    }

    @Test
    void shouldDeserializeValueObjectUsingFromFactoryMethod() {
        Map<String, Object> state = new HashMap<>();
        state.put("fromMethodObject", "from-value");

        var snapshot = SnapshotEvent.of(state);
        var metadata = createConstructorTestMetadata("test-id", 1);

        var aggregate = deserializer.deserialize(snapshot, ConstructorTestAggregate.init(), metadata);

        assertThat(aggregate.getFromMethodObject()).isNotNull();
        assertThat(aggregate.getFromMethodObject().getValue()).isEqualTo("from-value");
    }

    @Test
    void shouldDeserializeValueObjectUsingCreateFactoryMethod() {
        Map<String, Object> state = new HashMap<>();
        state.put("createMethodObject", "create-value");

        var snapshot = SnapshotEvent.of(state);
        var metadata = createConstructorTestMetadata("test-id", 1);

        var aggregate = deserializer.deserialize(snapshot, ConstructorTestAggregate.init(), metadata);

        assertThat(aggregate.getCreateMethodObject()).isNotNull();
        assertThat(aggregate.getCreateMethodObject().getValue()).isEqualTo("create-value");
    }

    @Test
    void shouldDeserializeComplexObjectUsingConstructorWhenNoFactoryMethod() {
        Map<String, Object> state = new HashMap<>();
        state.put("constructorOnlyComplex", Map.of("name", "test-name", "count", 42));

        var snapshot = SnapshotEvent.of(state);
        var metadata = createConstructorTestMetadata("test-id", 1);

        var aggregate = deserializer.deserialize(snapshot, ConstructorTestAggregate.init(), metadata);

        assertThat(aggregate.getConstructorOnlyComplex()).isNotNull();
        assertThat(aggregate.getConstructorOnlyComplex().getName()).isEqualTo("test-name");
        assertThat(aggregate.getConstructorOnlyComplex().getCount()).isEqualTo(42);
    }

    @Test
    void shouldDeserializeIdUsingConstructorWhenNoOfMethod() {
        Map<String, Object> state = new HashMap<>();

        var snapshot = SnapshotEvent.of(state);
        var metadata = createConstructorIdMetadata("constructor-id", 5);

        var aggregate = deserializer.deserialize(snapshot, ConstructorIdAggregate.init(), metadata);

        assertThat(aggregate.getId()).isNotNull();
        assertThat(aggregate.getId().getValue()).isEqualTo("constructor-id");
    }

    private EventMetadata createConstructorTestMetadata(String aggregateId, long version) {
        return EventMetadata.of(
                AggregateId.of(aggregateId),
                AggregateType.of("CONSTRUCTOR_TEST"),
                Version.of(version),
                Agent.system(),
                Instant.now(),
                true
        );
    }

    private EventMetadata createConstructorIdMetadata(String aggregateId, long version) {
        return EventMetadata.of(
                AggregateId.of(aggregateId),
                AggregateType.of("CONSTRUCTOR_ID_TEST"),
                Version.of(version),
                Agent.system(),
                Instant.now(),
                true
        );
    }

    @Value
    static class ConstructorOnlyValueObject {

        String value;

        public ConstructorOnlyValueObject(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

    @Value
    static class FromMethodValueObject {

        String value;

        private FromMethodValueObject(String value) {
            this.value = value;
        }

        public static FromMethodValueObject from(String value) {
            return new FromMethodValueObject(value);
        }

        public String getValue() {
            return value;
        }

    }

    @Value
    static class CreateMethodValueObject {

        String value;

        private CreateMethodValueObject(String value) {
            this.value = value;
        }

        public static CreateMethodValueObject create(String value) {
            return new CreateMethodValueObject(value);
        }

        public String getValue() {
            return value;
        }

    }

    @Value
    static class ConstructorOnlyComplexObject {

        String name;

        int count;

        public ConstructorOnlyComplexObject(String name, int count) {
            this.name = name;
            this.count = count;
        }

    }

    @Value
    static class ConstructorOnlyId {

        String value;

        public ConstructorOnlyId(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

    @Value
    @With(PRIVATE)
    static class ConstructorTestAggregate implements Aggregate {

        public static final AggregateType TYPE = AggregateType.of("CONSTRUCTOR_TEST");

        @SnapshotExclude
        String id;

        @SnapshotExclude
        Version version;

        ConstructorOnlyValueObject constructorOnlyObject;

        FromMethodValueObject fromMethodObject;

        CreateMethodValueObject createMethodObject;

        ConstructorOnlyComplexObject constructorOnlyComplex;

        public static ConstructorTestAggregate init() {
            return new ConstructorTestAggregate(null, null, null, null, null, null);
        }

        @Override
        public ApplyCommandResult apply(Command cmd, Agent agent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Aggregate apply(Event event, EventMetadata metadata) {
            throw new UnsupportedOperationException();
        }

    }

    @Value
    @With(PRIVATE)
    static class ConstructorIdAggregate implements Aggregate {

        public static final AggregateType TYPE = AggregateType.of("CONSTRUCTOR_ID_TEST");

        @SnapshotExclude
        ConstructorOnlyId id;

        @SnapshotExclude
        Version version;

        public static ConstructorIdAggregate init() {
            return new ConstructorIdAggregate(null, null);
        }

        @Override
        public ApplyCommandResult apply(Command cmd, Agent agent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Aggregate apply(Event event, EventMetadata metadata) {
            throw new UnsupportedOperationException();
        }

    }

}
