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
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.AggregateSnapshotSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotExclude;
import jakarta.annotation.Nullable;
import lombok.Value;
import lombok.With;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;

public class AggregateSnapshotRoundtripTest {

    private final AggregateSnapshotSerializer serializer = new AggregateSnapshotSerializer();
    private final AggregateSnapshotDeserializer deserializer = new AggregateSnapshotDeserializer();

    @Test
    void shouldRoundtripAllTypes() {
        var original = TestAggregate.of(
                "aggregate-id",
                Version.of(42),
                "test string",
                123,
                9876543210L,
                3.14159,
                2.718f,
                true,
                Instant.parse("2024-06-15T14:30:00Z"),
                Duration.ofMinutes(90),
                TestStatus.PENDING,
                List.of("apple", "banana", "cherry"),
                Set.of("x", "y", "z"),
                Map.of("color", "blue", "size", "large"),
                TestValueObject.of("wrapped value"),
                TestNestedObject.of("nested item", 7),
                List.of(TestValueObject.of("first"), TestValueObject.of("second"))
        );

        var snapshot = serializer.serialize(original);
        var metadata = createMetadata("aggregate-id", 42);
        var restored = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getVersion()).isEqualTo(original.getVersion());
        assertThat(restored.getStringValue()).isEqualTo(original.getStringValue());
        assertThat(restored.getIntValue()).isEqualTo(original.getIntValue());
        assertThat(restored.getLongValue()).isEqualTo(original.getLongValue());
        assertThat(restored.getDoubleValue()).isEqualTo(original.getDoubleValue());
        assertThat(restored.getFloatValue()).isEqualTo(original.getFloatValue());
        assertThat(restored.isBooleanValue()).isEqualTo(original.isBooleanValue());
        assertThat(restored.getInstantValue()).isEqualTo(original.getInstantValue());
        assertThat(restored.getDurationValue()).isEqualTo(original.getDurationValue());
        assertThat(restored.getEnumValue()).isEqualTo(original.getEnumValue());
        assertThat(restored.getListValue()).containsExactlyElementsOf(original.getListValue());
        assertThat(restored.getSetValue()).containsExactlyInAnyOrderElementsOf(original.getSetValue());
        assertThat(restored.getMapValue()).containsExactlyEntriesOf(original.getMapValue());
        assertThat(restored.getValueObject().getValue()).isEqualTo(original.getValueObject().getValue());
        assertThat(restored.getNestedObject().getName()).isEqualTo(original.getNestedObject().getName());
        assertThat(restored.getNestedObject().getCount()).isEqualTo(original.getNestedObject().getCount());
        assertThat(restored.getValueObjectList()).hasSize(2);
        assertThat(restored.getValueObjectList().get(0).getValue()).isEqualTo("first");
        assertThat(restored.getValueObjectList().get(1).getValue()).isEqualTo("second");
    }

    @Test
    void shouldRoundtripWithNullValues() {
        var original = TestAggregate.of(
                "aggregate-id",
                Version.of(5),
                null,
                0,
                0L,
                0.0,
                0.0f,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        var snapshot = serializer.serialize(original);
        var metadata = createMetadata("aggregate-id", 5);
        var restored = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getVersion()).isEqualTo(original.getVersion());
        assertThat(restored.getStringValue()).isNull();
        assertThat(restored.getInstantValue()).isNull();
        assertThat(restored.getDurationValue()).isNull();
        assertThat(restored.getEnumValue()).isNull();
        assertThat(restored.getListValue()).isNull();
        assertThat(restored.getSetValue()).isNull();
        assertThat(restored.getMapValue()).isNull();
        assertThat(restored.getValueObject()).isNull();
        assertThat(restored.getNestedObject()).isNull();
        assertThat(restored.getValueObjectList()).isNull();
    }

    @Test
    void shouldRoundtripWithEmptyCollections() {
        var original = TestAggregate.of(
                "aggregate-id",
                Version.zero(),
                "",
                0,
                0L,
                0.0,
                0.0f,
                false,
                null,
                null,
                null,
                List.of(),
                Set.of(),
                Map.of(),
                null,
                null,
                List.of()
        );

        var snapshot = serializer.serialize(original);
        var metadata = createMetadata("aggregate-id", 0);
        var restored = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(restored.getListValue()).isEmpty();
        assertThat(restored.getSetValue()).isEmpty();
        assertThat(restored.getMapValue()).isEmpty();
        assertThat(restored.getValueObjectList()).isEmpty();
    }

    @Test
    void shouldPreserveIdAndVersionFromMetadata() {
        var original = TestAggregate.of(
                "original-id",
                Version.of(100),
                "test",
                0,
                0L,
                0.0,
                0.0f,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        var snapshot = serializer.serialize(original);

        var metadata = createMetadata("different-id", 200);
        var restored = deserializer.deserialize(snapshot, TestAggregate.init(), metadata);

        assertThat(restored.getId()).isEqualTo("different-id");
        assertThat(restored.getVersion()).isEqualTo(Version.of(200));
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

        public static TestAggregate of(
                String id,
                Version version,
                String stringValue,
                int intValue,
                long longValue,
                double doubleValue,
                float floatValue,
                boolean booleanValue,
                Instant instantValue,
                Duration durationValue,
                TestStatus enumValue,
                List<String> listValue,
                Set<String> setValue,
                Map<String, String> mapValue,
                TestValueObject valueObject,
                TestNestedObject nestedObject,
                List<TestValueObject> valueObjectList
        ) {
            return new TestAggregate(
                    id,
                    version,
                    stringValue,
                    intValue,
                    longValue,
                    doubleValue,
                    floatValue,
                    booleanValue,
                    instantValue,
                    durationValue,
                    enumValue,
                    listValue,
                    setValue,
                    mapValue,
                    valueObject,
                    nestedObject,
                    valueObjectList
            );
        }

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

        public static TypedIdAggregate of(
                TestId id,
                Version version,
                TestId referenceId,
                Set<TestId> idSet,
                Set<TestAvailability> availabilities
        ) {
            return new TypedIdAggregate(id, version, referenceId, idSet, availabilities);
        }

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
    void shouldRoundtripTypedIdAggregate() {
        var original = TypedIdAggregate.of(
                TestId.of("aggregate-id"),
                Version.of(42),
                TestId.of("ref-123"),
                Set.of(TestId.of("id-1"), TestId.of("id-2"), TestId.of("id-3")),
                Set.of(
                        TestAvailability.of(TestId.of("type-a"), true),
                        TestAvailability.of(TestId.of("type-b"), false),
                        TestAvailability.of(TestId.of("type-c"), true)
                )
        );

        var snapshot = serializer.serialize(original);
        var metadata = createTypedIdMetadata("aggregate-id", 42);
        var restored = deserializer.deserialize(snapshot, TypedIdAggregate.init(), metadata);

        assertThat(restored.getId().getValue()).isEqualTo(original.getId().getValue());
        assertThat(restored.getVersion()).isEqualTo(original.getVersion());
        assertThat(restored.getReferenceId().getValue()).isEqualTo(original.getReferenceId().getValue());

        var originalIdValues = original.getIdSet().stream().map(TestId::getValue).collect(java.util.stream.Collectors.toSet());
        var restoredIdValues = restored.getIdSet().stream().map(TestId::getValue).collect(java.util.stream.Collectors.toSet());
        assertThat(restoredIdValues).containsExactlyInAnyOrderElementsOf(originalIdValues);

        var originalAvailabilities = original.getAvailabilities().stream()
                .collect(java.util.stream.Collectors.toMap(a -> a.getTypeId().getValue(), TestAvailability::isInStock));
        var restoredAvailabilities = restored.getAvailabilities().stream()
                .collect(java.util.stream.Collectors.toMap(a -> a.getTypeId().getValue(), TestAvailability::isInStock));
        assertThat(restoredAvailabilities).containsExactlyInAnyOrderEntriesOf(originalAvailabilities);
    }

    @Test
    void shouldRoundtripTypedIdAggregateWithNullCollections() {
        var original = TypedIdAggregate.of(
                TestId.of("aggregate-id"),
                Version.of(5),
                null,
                null,
                null
        );

        var snapshot = serializer.serialize(original);
        var metadata = createTypedIdMetadata("aggregate-id", 5);
        var restored = deserializer.deserialize(snapshot, TypedIdAggregate.init(), metadata);

        assertThat(restored.getId().getValue()).isEqualTo("aggregate-id");
        assertThat(restored.getVersion()).isEqualTo(Version.of(5));
        assertThat(restored.getReferenceId()).isNull();
        assertThat(restored.getIdSet()).isNull();
        assertThat(restored.getAvailabilities()).isNull();
    }

    @Test
    void shouldRoundtripTypedIdAggregateWithEmptyCollections() {
        var original = TypedIdAggregate.of(
                TestId.of("aggregate-id"),
                Version.zero(),
                TestId.of("ref"),
                Set.of(),
                Set.of()
        );

        var snapshot = serializer.serialize(original);
        var metadata = createTypedIdMetadata("aggregate-id", 0);
        var restored = deserializer.deserialize(snapshot, TypedIdAggregate.init(), metadata);

        assertThat(restored.getReferenceId().getValue()).isEqualTo("ref");
        assertThat(restored.getIdSet()).isEmpty();
        assertThat(restored.getAvailabilities()).isEmpty();
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

}
