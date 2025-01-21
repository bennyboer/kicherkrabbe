package de.bennyboer.kicherkrabbe.products.persistence;

import de.bennyboer.kicherkrabbe.products.counter.increment.IncrementedEvent;
import de.bennyboer.kicherkrabbe.products.counter.init.InitEvent;
import de.bennyboer.kicherkrabbe.products.counter.snapshot.SnapshottedEvent;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CounterEventPayloadSerializerTest {

    private final CounterEventPayloadSerializer serializer = new CounterEventPayloadSerializer();

    @Test
    void shouldSerializeAndDeserializeCreatedEvent() {
        // when: an init event is serialized
        var event = InitEvent.of();
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEqualTo(Map.of());

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(InitEvent.NAME, InitEvent.VERSION, serialized);

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeSnapshottedEvent() {
        // when: a snapshotted event is serialized
        var event = SnapshottedEvent.of(42L);
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEqualTo(Map.of(
                "value", 42L
        ));

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(SnapshottedEvent.NAME, SnapshottedEvent.VERSION, serialized);

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

    @Test
    void shouldSerializeAndDeserializeIncrementedEvent() {
        // when: an incremented event is serialized
        var event = IncrementedEvent.of();
        var serialized = serializer.serialize(event);

        // then: the serialized form is correct
        assertThat(serialized).isEqualTo(Map.of());

        // when: the serialized form is deserialized
        var deserialized = serializer.deserialize(IncrementedEvent.NAME, IncrementedEvent.VERSION, serialized);

        // then: the deserialized form is correct
        assertThat(deserialized).isEqualTo(event);
    }

}
