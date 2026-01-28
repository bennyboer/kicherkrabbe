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
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.publish.LoggingEventPublisher;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.AggregateSnapshotDeserializer;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.AggregateSnapshotSerializer;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotEvent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot.SnapshotExclude;
import de.bennyboer.kicherkrabbe.eventsourcing.example.SampleAggregateService;
import de.bennyboer.kicherkrabbe.eventsourcing.persistence.events.inmemory.InMemoryEventSourcingRepo;
import jakarta.annotation.Nullable;
import lombok.Value;
import lombok.With;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
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

    @Test
    void shouldCompareAutomaticVsManualSnapshotPerformance() {
        var eventPublisher = new LoggingEventPublisher();
        var testAgent = Agent.user(AgentId.of("TEST_USER"));

        int eventCount = 500;
        int loadIterations = 1000;

        var autoSnapshotRepo = new InMemoryEventSourcingRepo();
        var autoSnapshotService = new SampleAggregateService(autoSnapshotRepo, eventPublisher, Clock.systemUTC());

        var autoId = "AUTO_SNAPSHOT_ID";
        var autoVersion = autoSnapshotService.create(autoId, "Title", "Description", testAgent).block();
        for (int i = 0; i < eventCount; i++) {
            autoVersion = autoSnapshotService.updateTitle(autoId, autoVersion, "Title " + i, testAgent).block();
        }

        var manualSnapshotRepo = new InMemoryEventSourcingRepo();
        var manualSnapshotService = new ManualSnapshotAggregateService(manualSnapshotRepo, eventPublisher, Clock.systemUTC());

        var manualId = "MANUAL_SNAPSHOT_ID";
        var manualVersion = manualSnapshotService.create(manualId, "Title", "Description", testAgent).block();
        for (int i = 0; i < eventCount; i++) {
            manualVersion = manualSnapshotService.updateTitle(manualId, manualVersion, "Title " + i, testAgent).block();
        }
        manualSnapshotService.snapshot(manualId, manualVersion, testAgent).block();

        for (int i = 0; i < 100; i++) {
            autoSnapshotService.get(autoId).block();
            manualSnapshotService.get(manualId).block();
        }

        long autoStartNanos = System.nanoTime();
        for (int i = 0; i < loadIterations; i++) {
            autoSnapshotService.get(autoId).block();
        }
        long autoEndNanos = System.nanoTime();
        long autoTotalMicros = (autoEndNanos - autoStartNanos) / 1000;
        long autoAvgMicros = autoTotalMicros / loadIterations;

        long manualStartNanos = System.nanoTime();
        for (int i = 0; i < loadIterations; i++) {
            manualSnapshotService.get(manualId).block();
        }
        long manualEndNanos = System.nanoTime();
        long manualTotalMicros = (manualEndNanos - manualStartNanos) / 1000;
        long manualAvgMicros = manualTotalMicros / loadIterations;

        System.out.println("=== Automatic vs Manual Snapshot Performance ===");
        System.out.printf("Event count: %d, Load iterations: %d%n", eventCount, loadIterations);
        System.out.println();
        System.out.printf("Automatic snapshots (reflection-based):%n");
        System.out.printf("  Total: %d µs, Avg: %d µs/load%n", autoTotalMicros, autoAvgMicros);
        System.out.println();
        System.out.printf("Manual snapshots (dedicated event):%n");
        System.out.printf("  Total: %d µs, Avg: %d µs/load%n", manualTotalMicros, manualAvgMicros);
        System.out.println();

        if (manualAvgMicros > 0 && autoAvgMicros > 0) {
            double ratio = (double) autoAvgMicros / manualAvgMicros;
            System.out.printf("Ratio (auto/manual): %.2fx%n", ratio);
            if (ratio > 1) {
                System.out.printf("Manual snapshots are %.1f%% faster%n", (ratio - 1) * 100);
            } else {
                System.out.printf("Automatic snapshots are %.1f%% faster%n", (1 / ratio - 1) * 100);
            }
        }
    }

    @Test
    void shouldCompareAutomaticVsManualSnapshotPerformanceForComplexAggregate() {
        var aggregate = createComplexAggregate();
        var metadata = createMetadata("complex-perf-test", 100);

        int iterations = TEST_ITERATIONS;

        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            var snapshot = serializer.serialize(aggregate);
            deserializer.deserialize(snapshot, ComplexAggregate.init(), metadata);

            var manualSnapshot = serializeComplexAggregateManually(aggregate);
            deserializeComplexAggregateManually(manualSnapshot, metadata);
        }

        long autoSerStartNanos = System.nanoTime();
        SnapshotEvent autoSnapshot = null;
        for (int i = 0; i < iterations; i++) {
            autoSnapshot = serializer.serialize(aggregate);
        }
        long autoSerEndNanos = System.nanoTime();
        long autoSerTotalMicros = (autoSerEndNanos - autoSerStartNanos) / 1000;
        long autoSerAvgMicros = autoSerTotalMicros / iterations;

        long autoDeserStartNanos = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            deserializer.deserialize(autoSnapshot, ComplexAggregate.init(), metadata);
        }
        long autoDeserEndNanos = System.nanoTime();
        long autoDeserTotalMicros = (autoDeserEndNanos - autoDeserStartNanos) / 1000;
        long autoDeserAvgMicros = autoDeserTotalMicros / iterations;

        long manualSerStartNanos = System.nanoTime();
        SnapshotEvent manualSnapshot = null;
        for (int i = 0; i < iterations; i++) {
            manualSnapshot = serializeComplexAggregateManually(aggregate);
        }
        long manualSerEndNanos = System.nanoTime();
        long manualSerTotalMicros = (manualSerEndNanos - manualSerStartNanos) / 1000;
        long manualSerAvgMicros = manualSerTotalMicros / iterations;

        long manualDeserStartNanos = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            deserializeComplexAggregateManually(manualSnapshot, metadata);
        }
        long manualDeserEndNanos = System.nanoTime();
        long manualDeserTotalMicros = (manualDeserEndNanos - manualDeserStartNanos) / 1000;
        long manualDeserAvgMicros = manualDeserTotalMicros / iterations;

        System.out.println("=== Complex Aggregate: Automatic vs Manual Snapshot Performance ===");
        System.out.printf("Iterations: %d%n", iterations);
        System.out.println();
        System.out.println("SERIALIZATION:");
        System.out.printf("  Automatic (reflection): Total %d µs, Avg %d µs/op%n", autoSerTotalMicros, autoSerAvgMicros);
        System.out.printf("  Manual (explicit):      Total %d µs, Avg %d µs/op%n", manualSerTotalMicros, manualSerAvgMicros);
        if (manualSerAvgMicros > 0) {
            double serRatio = (double) autoSerAvgMicros / manualSerAvgMicros;
            System.out.printf("  Ratio (auto/manual): %.2fx%n", serRatio);
        }
        System.out.println();
        System.out.println("DESERIALIZATION:");
        System.out.printf("  Automatic (reflection): Total %d µs, Avg %d µs/op%n", autoDeserTotalMicros, autoDeserAvgMicros);
        System.out.printf("  Manual (explicit):      Total %d µs, Avg %d µs/op%n", manualDeserTotalMicros, manualDeserAvgMicros);
        if (manualDeserAvgMicros > 0) {
            double deserRatio = (double) autoDeserAvgMicros / manualDeserAvgMicros;
            System.out.printf("  Ratio (auto/manual): %.2fx%n", deserRatio);
        }
        System.out.println();

        long autoTotalAvg = autoSerAvgMicros + autoDeserAvgMicros;
        long manualTotalAvg = manualSerAvgMicros + manualDeserAvgMicros;
        System.out.println("TOTAL (serialization + deserialization):");
        System.out.printf("  Automatic (reflection): Avg %d µs/roundtrip%n", autoTotalAvg);
        System.out.printf("  Manual (explicit):      Avg %d µs/roundtrip%n", manualTotalAvg);
        if (manualTotalAvg > 0) {
            double totalRatio = (double) autoTotalAvg / manualTotalAvg;
            System.out.printf("  Ratio (auto/manual): %.2fx%n", totalRatio);
            if (totalRatio > 1) {
                System.out.printf("  Manual snapshots are %.1f%% faster overall%n", (totalRatio - 1) * 100);
            } else {
                System.out.printf("  Automatic snapshots are %.1f%% faster overall%n", (1 / totalRatio - 1) * 100);
            }
        }
    }

    private SnapshotEvent serializeComplexAggregateManually(ComplexAggregate aggregate) {
        Map<String, Object> state = new HashMap<>();
        state.put("name", aggregate.getName());
        state.put("description", aggregate.getDescription());
        state.put("intValue", aggregate.getIntValue());
        state.put("longValue", aggregate.getLongValue());
        state.put("doubleValue", aggregate.getDoubleValue());
        state.put("booleanValue", aggregate.isBooleanValue());
        state.put("instantValue", aggregate.getInstantValue().toString());
        state.put("durationValue", aggregate.getDurationValue().toString());
        state.put("status", aggregate.getStatus().name());
        state.put("stringList", aggregate.getStringList());
        state.put("idSet", aggregate.getIdSet().stream().map(PerfTestId::getValue).toList());
        state.put("stringMap", aggregate.getStringMap());
        state.put("nestedObject", Map.of(
                "name", aggregate.getNestedObject().getName(),
                "count", aggregate.getNestedObject().getCount()
        ));
        state.put("availabilities", aggregate.getAvailabilities().stream()
                .map(a -> Map.of("typeId", a.getTypeId().getValue(), "available", a.isAvailable()))
                .toList());
        state.put("createdAt", aggregate.getCreatedAt().toString());
        if (aggregate.getDeletedAt() != null) {
            state.put("deletedAt", aggregate.getDeletedAt().toString());
        }
        return SnapshotEvent.of(state);
    }

    @SuppressWarnings("unchecked")
    private ComplexAggregate deserializeComplexAggregateManually(SnapshotEvent snapshot, EventMetadata metadata) {
        Map<String, Object> state = snapshot.getState();
        return ComplexAggregate.of(
                PerfTestId.of(metadata.getAggregateId().getValue()),
                metadata.getAggregateVersion(),
                (String) state.get("name"),
                (String) state.get("description"),
                ((Number) state.get("intValue")).intValue(),
                ((Number) state.get("longValue")).longValue(),
                ((Number) state.get("doubleValue")).doubleValue(),
                (Boolean) state.get("booleanValue"),
                Instant.parse((String) state.get("instantValue")),
                Duration.parse((String) state.get("durationValue")),
                PerfStatus.valueOf((String) state.get("status")),
                (List<String>) state.get("stringList"),
                ((List<String>) state.get("idSet")).stream().map(PerfTestId::of).collect(java.util.stream.Collectors.toSet()),
                (Map<String, String>) state.get("stringMap"),
                deserializeNestedObject((Map<String, Object>) state.get("nestedObject")),
                ((List<Map<String, Object>>) state.get("availabilities")).stream()
                        .map(this::deserializeAvailability)
                        .collect(java.util.stream.Collectors.toSet()),
                Instant.parse((String) state.get("createdAt")),
                state.containsKey("deletedAt") ? Instant.parse((String) state.get("deletedAt")) : null
        );
    }

    private PerfNestedObject deserializeNestedObject(Map<String, Object> map) {
        return PerfNestedObject.of(
                (String) map.get("name"),
                ((Number) map.get("count")).intValue()
        );
    }

    private PerfAvailability deserializeAvailability(Map<String, Object> map) {
        return PerfAvailability.of(
                PerfTestId.of((String) map.get("typeId")),
                (Boolean) map.get("available")
        );
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
