package de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class AggregateSnapshotSerializer {

    public SnapshotEvent serialize(Aggregate aggregate) {
        Map<String, Object> state = new LinkedHashMap<>();

        for (Field field : getAllFields(aggregate.getClass())) {
            if (shouldExclude(field)) {
                continue;
            }

            try {
                field.setAccessible(true);
                Object value = field.get(aggregate);
                Object serializedValue = serializeValue(value);
                state.put(field.getName(), serializedValue);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + field.getName(), e);
            }
        }

        return SnapshotEvent.of(state);
    }

    private Object serializeValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String
                || value instanceof Boolean
                || value instanceof Integer
                || value instanceof Long
                || value instanceof Double
                || value instanceof Float) {
            return value;
        }

        if (value instanceof Instant instant) {
            return instant.toString();
        }

        if (value instanceof Duration duration) {
            return duration.toString();
        }

        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }

        if (value instanceof Collection<?> collection) {
            return collection.stream()
                    .map(this::serializeValue)
                    .toList();
        }

        if (value instanceof Map<?, ?> map) {
            Map<String, Object> serializedMap = new LinkedHashMap<>();
            map.forEach((k, v) -> serializedMap.put(k.toString(), serializeValue(v)));
            return serializedMap;
        }

        return serializeObject(value);
    }

    private Object serializeObject(Object obj) {
        Method getValueMethod = findGetValueMethod(obj.getClass());
        if (getValueMethod != null) {
            try {
                getValueMethod.setAccessible(true);
                return serializeValue(getValueMethod.invoke(obj));
            } catch (Exception e) {
                throw new RuntimeException("Failed to invoke getValue() on " + obj.getClass().getName(), e);
            }
        }

        Map<String, Object> serialized = new LinkedHashMap<>();
        for (Field field : getAllFields(obj.getClass())) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                serialized.put(field.getName(), serializeValue(value));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + field.getName(), e);
            }
        }
        return serialized;
    }

    private Method findGetValueMethod(Class<?> clazz) {
        try {
            return clazz.getMethod("getValue");
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private boolean shouldExclude(Field field) {
        return field.isAnnotationPresent(SnapshotExclude.class);
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        return fields.stream()
                .filter(f -> !f.isSynthetic())
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .toList();
    }

}
