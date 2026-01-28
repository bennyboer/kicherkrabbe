package de.bennyboer.kicherkrabbe.eventsourcing.example.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateType;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.ApplyCommandResult;
import de.bennyboer.kicherkrabbe.eventsourcing.command.Command;
import de.bennyboer.kicherkrabbe.eventsourcing.event.Event;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
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

public class AggregateSnapshotSerializerTest {

    private final AggregateSnapshotSerializer serializer = new AggregateSnapshotSerializer();

    @Test
    void shouldSerializePrimitiveTypes() {
        var aggregate = TestAggregate.of(
                "test-id",
                Version.of(5),
                "test string",
                42,
                123456789L,
                3.14,
                2.5f,
                true,
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

        var snapshot = serializer.serialize(aggregate);
        var state = snapshot.getState();

        assertThat(state.get("stringValue")).isEqualTo("test string");
        assertThat(state.get("intValue")).isEqualTo(42);
        assertThat(state.get("longValue")).isEqualTo(123456789L);
        assertThat(state.get("doubleValue")).isEqualTo(3.14);
        assertThat(state.get("floatValue")).isEqualTo(2.5f);
        assertThat(state.get("booleanValue")).isEqualTo(true);
    }

    @Test
    void shouldSerializeInstant() {
        var instant = Instant.parse("2024-01-15T10:30:00Z");
        var aggregate = TestAggregate.of(
                "test-id",
                Version.zero(),
                null,
                0,
                0L,
                0.0,
                0.0f,
                false,
                instant,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        var snapshot = serializer.serialize(aggregate);
        var state = snapshot.getState();

        assertThat(state.get("instantValue")).isEqualTo("2024-01-15T10:30:00Z");
    }

    @Test
    void shouldSerializeDuration() {
        var duration = Duration.ofHours(2).plusMinutes(30);
        var aggregate = TestAggregate.of(
                "test-id",
                Version.zero(),
                null,
                0,
                0L,
                0.0,
                0.0f,
                false,
                null,
                duration,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        var snapshot = serializer.serialize(aggregate);
        var state = snapshot.getState();

        assertThat(state.get("durationValue")).isEqualTo("PT2H30M");
    }

    @Test
    void shouldSerializeEnum() {
        var aggregate = TestAggregate.of(
                "test-id",
                Version.zero(),
                null,
                0,
                0L,
                0.0,
                0.0f,
                false,
                null,
                null,
                TestStatus.ACTIVE,
                null,
                null,
                null,
                null,
                null,
                null
        );

        var snapshot = serializer.serialize(aggregate);
        var state = snapshot.getState();

        assertThat(state.get("enumValue")).isEqualTo("ACTIVE");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSerializeList() {
        var aggregate = TestAggregate.of(
                "test-id",
                Version.zero(),
                null,
                0,
                0L,
                0.0,
                0.0f,
                false,
                null,
                null,
                null,
                List.of("item1", "item2", "item3"),
                null,
                null,
                null,
                null,
                null
        );

        var snapshot = serializer.serialize(aggregate);
        var state = snapshot.getState();

        var list = (List<String>) state.get("listValue");
        assertThat(list).containsExactly("item1", "item2", "item3");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSerializeSet() {
        var aggregate = TestAggregate.of(
                "test-id",
                Version.zero(),
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
                Set.of("a", "b", "c"),
                null,
                null,
                null,
                null
        );

        var snapshot = serializer.serialize(aggregate);
        var state = snapshot.getState();

        var set = (List<String>) state.get("setValue");
        assertThat(set).containsExactlyInAnyOrder("a", "b", "c");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSerializeMap() {
        var aggregate = TestAggregate.of(
                "test-id",
                Version.zero(),
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
                Map.of("key1", "value1", "key2", "value2"),
                null,
                null,
                null
        );

        var snapshot = serializer.serialize(aggregate);
        var state = snapshot.getState();

        var map = (Map<String, String>) state.get("mapValue");
        assertThat(map).containsEntry("key1", "value1");
        assertThat(map).containsEntry("key2", "value2");
    }

    @Test
    void shouldSerializeValueObject() {
        var valueObject = TestValueObject.of("inner value");
        var aggregate = TestAggregate.of(
                "test-id",
                Version.zero(),
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
                valueObject,
                null,
                null
        );

        var snapshot = serializer.serialize(aggregate);
        var state = snapshot.getState();

        assertThat(state.get("valueObject")).isEqualTo("inner value");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSerializeNestedObject() {
        var nested = TestNestedObject.of("nested name", 99);
        var aggregate = TestAggregate.of(
                "test-id",
                Version.zero(),
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
                nested,
                null
        );

        var snapshot = serializer.serialize(aggregate);
        var state = snapshot.getState();

        var nestedMap = (Map<String, Object>) state.get("nestedObject");
        assertThat(nestedMap.get("name")).isEqualTo("nested name");
        assertThat(nestedMap.get("count")).isEqualTo(99);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSerializeListOfValueObjects() {
        var items = List.of(
                TestValueObject.of("first"),
                TestValueObject.of("second")
        );
        var aggregate = TestAggregate.of(
                "test-id",
                Version.zero(),
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
                items
        );

        var snapshot = serializer.serialize(aggregate);
        var state = snapshot.getState();

        var list = (List<String>) state.get("valueObjectList");
        assertThat(list).containsExactly("first", "second");
    }

    @Test
    void shouldExcludeFieldsWithSnapshotExcludeAnnotation() {
        var aggregate = TestAggregate.of(
                "excluded-id",
                Version.of(10),
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

        var snapshot = serializer.serialize(aggregate);
        var state = snapshot.getState();

        assertThat(state).doesNotContainKey("id");
        assertThat(state).doesNotContainKey("version");
    }

    @Test
    void shouldSerializeNullValues() {
        var aggregate = TestAggregate.of(
                "test-id",
                Version.zero(),
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

        var snapshot = serializer.serialize(aggregate);
        var state = snapshot.getState();

        assertThat(state.get("stringValue")).isNull();
        assertThat(state.get("instantValue")).isNull();
        assertThat(state.get("durationValue")).isNull();
        assertThat(state.get("enumValue")).isNull();
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
    void shouldSerializeTypedId() {
        var aggregate = TypedIdAggregate.of(
                TestId.of("aggregate-id"),
                Version.of(5),
                TestId.of("reference-123"),
                null,
                null
        );

        var snapshot = serializer.serialize(aggregate);
        var state = snapshot.getState();

        assertThat(state.get("referenceId")).isEqualTo("reference-123");
        assertThat(state).doesNotContainKey("id");
        assertThat(state).doesNotContainKey("version");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSerializeSetOfTypedIds() {
        var aggregate = TypedIdAggregate.of(
                TestId.of("aggregate-id"),
                Version.of(5),
                null,
                Set.of(TestId.of("id-1"), TestId.of("id-2"), TestId.of("id-3")),
                null
        );

        var snapshot = serializer.serialize(aggregate);
        var state = snapshot.getState();

        var idSet = (List<String>) state.get("idSet");
        assertThat(idSet).containsExactlyInAnyOrder("id-1", "id-2", "id-3");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSerializeSetOfComplexNestedObjects() {
        var availabilities = Set.of(
                TestAvailability.of(TestId.of("type-1"), true),
                TestAvailability.of(TestId.of("type-2"), false)
        );
        var aggregate = TypedIdAggregate.of(
                TestId.of("aggregate-id"),
                Version.of(5),
                null,
                null,
                availabilities
        );

        var snapshot = serializer.serialize(aggregate);
        var state = snapshot.getState();

        var availabilityList = (List<Map<String, Object>>) state.get("availabilities");
        assertThat(availabilityList).hasSize(2);

        var availabilityValues = availabilityList.stream()
                .map(m -> Map.entry(m.get("typeId"), m.get("inStock")))
                .collect(java.util.stream.Collectors.toSet());

        assertThat(availabilityValues).containsExactlyInAnyOrder(
                Map.entry("type-1", true),
                Map.entry("type-2", false)
        );
    }

}
