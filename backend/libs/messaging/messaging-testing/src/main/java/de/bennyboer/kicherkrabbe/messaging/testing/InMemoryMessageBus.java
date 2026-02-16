package de.bennyboer.kicherkrabbe.messaging.testing;

import de.bennyboer.kicherkrabbe.messaging.listener.AcknowledgableMessage;
import org.springframework.amqp.core.Message;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryMessageBus {

    private final List<RegisteredListener> listeners = new CopyOnWriteArrayList<>();

    public void register(String exchange, String routingKeyPattern, Sinks.Many<AcknowledgableMessage> sink) {
        listeners.add(new RegisteredListener(exchange, routingKeyPattern, sink));
    }

    public void publish(String exchange, String routingKey, Message message) {
        var ackMessage = new NoOpAcknowledgableMessage(message);

        for (var listener : listeners) {
            if (listener.exchange().equals(exchange) && matchesRoutingKey(listener.routingKeyPattern(), routingKey)) {
                var result = listener.sink().tryEmitNext(ackMessage);
                if (result == Sinks.EmitResult.FAIL_TERMINATED || result == Sinks.EmitResult.FAIL_CANCELLED) {
                    listeners.remove(listener);
                }
            }
        }
    }

    static boolean matchesRoutingKey(String pattern, String routingKey) {
        String[] patternParts = pattern.split("\\.");
        String[] keyParts = routingKey.split("\\.");
        return matchParts(patternParts, 0, keyParts, 0);
    }

    private static boolean matchParts(String[] pattern, int pi, String[] key, int ki) {
        if (pi == pattern.length && ki == key.length) {
            return true;
        }
        if (pi == pattern.length) {
            return false;
        }

        String p = pattern[pi];

        if (p.equals("#")) {
            if (pi == pattern.length - 1) {
                return true;
            }
            for (int i = ki; i <= key.length; i++) {
                if (matchParts(pattern, pi + 1, key, i)) {
                    return true;
                }
            }
            return false;
        }

        if (ki == key.length) {
            return false;
        }

        if (p.equals("*")) {
            return matchParts(pattern, pi + 1, key, ki + 1);
        }

        return p.equals(key[ki]) && matchParts(pattern, pi + 1, key, ki + 1);
    }

    private record RegisteredListener(String exchange, String routingKeyPattern, Sinks.Many<AcknowledgableMessage> sink) {
    }

}
