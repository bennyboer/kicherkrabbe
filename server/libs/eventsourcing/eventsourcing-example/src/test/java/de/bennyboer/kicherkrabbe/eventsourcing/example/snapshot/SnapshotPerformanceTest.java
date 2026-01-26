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
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotEvent;
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

public class SnapshotPerformanceTest {

    private static final int WARMUP_ITERATIONS = 1000;
    private static final int TEST_ITERATIONS = 10000;
    private static final long MAX_AVERAGE_SERIALIZATION_MICROS = 500;
    private static final long MAX_AVERAGE_DESERIALIZATION_MICROS = 500;

    private final AggregateSnapshotSerializer serializer = new AggregateSnapshotSerializer();
    private final AggregateSnapshotDeserializer deserializer = new AggregateSnapshotDeserializer();

    @Test
    void shouldSerializeWithinAcceptableTime() {
        var aggregate = createComplexAggregate();

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            serializer.serialize(aggregate);
        }

        long startNanos = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            serializer.serialize(aggregate);
        }
        long endNanos = System.nanoTime();

        long totalMicros = (endNanos - startNanos) / 1000;
        long avgMicros = totalMicros / TEST_ITERATIONS;

        System.out.printf("Serialization: %d iterations, total %d µs, avg %d µs/op%n",
                TEST_ITERATIONS, totalMicros, avgMicros);

        assertThat(avgMicros)
                .as("Average serialization time should be under %d µs", MAX_AVERAGE_SERIALIZATION_MICROS)
                .isLessThan(MAX_AVERAGE_SERIALIZATION_MICROS);
    }

    @Test
    void shouldDeserializeWithinAcceptableTime() {
        var aggregate = createComplexAggregate();
        var snapshot = serializer.serialize(aggregate);
        var metadata = createMetadata("perf-test-id", 100);

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            deserializer.deserialize(snapshot, ComplexAggregate.init(), metadata);
        }

        long startNanos = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            deserializer.deserialize(snapshot, ComplexAggregate.init(), metadata);
        }
        long endNanos = System.nanoTime();

        long totalMicros = (endNanos - startNanos) / 1000;
        long avgMicros = totalMicros / TEST_ITERATIONS;

        System.out.printf("Deserialization: %d iterations, total %d µs, avg %d µs/op%n",
                TEST_ITERATIONS, totalMicros, avgMicros);

        assertThat(avgMicros)
                .as("Average deserialization time should be under %d µs", MAX_AVERAGE_DESERIALIZATION_MICROS)
                .isLessThan(MAX_AVERAGE_DESERIALIZATION_MICROS);
    }

    @Test
    void shouldRoundtripWithinAcceptableTime() {
        var aggregate = createComplexAggregate();
        var metadata = createMetadata("perf-test-id", 100);

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            var snapshot = serializer.serialize(aggregate);
            deserializer.deserialize(snapshot, ComplexAggregate.init(), metadata);
        }

        long startNanos = System.nanoTime();
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            var snapshot = serializer.serialize(aggregate);
            deserializer.deserialize(snapshot, ComplexAggregate.init(), metadata);
        }
        long endNanos = System.nanoTime();

        long totalMicros = (endNanos - startNanos) / 1000;
        long avgMicros = totalMicros / TEST_ITERATIONS;

        System.out.printf("Roundtrip: %d iterations, total %d µs, avg %d µs/op%n",
                TEST_ITERATIONS, totalMicros, avgMicros);

        assertThat(avgMicros)
                .as("Average roundtrip time should be under %d µs",
                        MAX_AVERAGE_SERIALIZATION_MICROS + MAX_AVERAGE_DESERIALIZATION_MICROS)
                .isLessThan(MAX_AVERAGE_SERIALIZATION_MICROS + MAX_AVERAGE_DESERIALIZATION_MICROS);
    }

    private ComplexAggregate createComplexAggregate() {
        return ComplexAggregate.of(
                PerfTestId.of("test-aggregate-id"),
                Version.of(100),
                "Test Aggregate Name",
                "This is a longer description text that simulates real-world data",
                42,
                123456789L,
                3.14159,
                true,
                Instant.now(),
                Duration.ofHours(2).plusMinutes(30),
                PerfStatus.ACTIVE,
                List.of("item1", "item2", "item3", "item4", "item5"),
                Set.of(
                        PerfTestId.of("ref-1"),
                        PerfTestId.of("ref-2"),
                        PerfTestId.of("ref-3")
                ),
                Map.of("key1", "value1", "key2", "value2", "key3", "value3"),
                PerfNestedObject.of("nested-name", 999),
                Set.of(
                        PerfAvailability.of(PerfTestId.of("type-1"), true),
                        PerfAvailability.of(PerfTestId.of("type-2"), false),
                        PerfAvailability.of(PerfTestId.of("type-3"), true)
                ),
                Instant.now().minus(Duration.ofDays(30)),
                null
        );
    }

    private EventMetadata createMetadata(String aggregateId, long version) {
        return EventMetadata.of(
                AggregateId.of(aggregateId),
                AggregateType.of("PERF_TEST"),
                Version.of(version),
                Agent.system(),
                Instant.now(),
                true
        );
    }

    @Value
    static class PerfTestId {
        String value;

        public static PerfTestId of(String value) {
            return new PerfTestId(value);
        }

        public String getValue() {
            return value;
        }
    }

    enum PerfStatus {
        ACTIVE, INACTIVE, PENDING, DELETED
    }

    @Value
    static class PerfNestedObject {
        String name;
        int count;

        public static PerfNestedObject of(String name, int count) {
            return new PerfNestedObject(name, count);
        }
    }

    @Value
    static class PerfAvailability {
        PerfTestId typeId;
        boolean available;

        public static PerfAvailability of(PerfTestId typeId, boolean available) {
            return new PerfAvailability(typeId, available);
        }
    }

    @Value
    @With(PRIVATE)
    static class ComplexAggregate implements Aggregate {

        public static final AggregateType TYPE = AggregateType.of("PERF_TEST");

        @SnapshotExclude
        PerfTestId id;

        @SnapshotExclude
        Version version;

        String name;

        String description;

        int intValue;

        long longValue;

        double doubleValue;

        boolean booleanValue;

        Instant instantValue;

        Duration durationValue;

        PerfStatus status;

        List<String> stringList;

        Set<PerfTestId> idSet;

        Map<String, String> stringMap;

        PerfNestedObject nestedObject;

        Set<PerfAvailability> availabilities;

        Instant createdAt;

        @Nullable
        Instant deletedAt;

        public static ComplexAggregate of(
                PerfTestId id,
                Version version,
                String name,
                String description,
                int intValue,
                long longValue,
                double doubleValue,
                boolean booleanValue,
                Instant instantValue,
                Duration durationValue,
                PerfStatus status,
                List<String> stringList,
                Set<PerfTestId> idSet,
                Map<String, String> stringMap,
                PerfNestedObject nestedObject,
                Set<PerfAvailability> availabilities,
                Instant createdAt,
                Instant deletedAt
        ) {
            return new ComplexAggregate(
                    id, version, name, description, intValue, longValue, doubleValue,
                    booleanValue, instantValue, durationValue, status, stringList, idSet,
                    stringMap, nestedObject, availabilities, createdAt, deletedAt
            );
        }

        public static ComplexAggregate init() {
            return new ComplexAggregate(
                    null, null, null, null, 0, 0L, 0.0, false, null, null,
                    null, null, null, null, null, null, null, null
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
}
