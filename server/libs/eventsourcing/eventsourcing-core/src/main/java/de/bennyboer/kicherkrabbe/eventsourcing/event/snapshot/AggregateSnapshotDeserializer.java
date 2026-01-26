package de.bennyboer.kicherkrabbe.eventsourcing.event.snapshot;

import de.bennyboer.kicherkrabbe.eventsourcing.Version;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.Aggregate;
import de.bennyboer.kicherkrabbe.eventsourcing.aggregate.AggregateId;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.EventMetadata;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class AggregateSnapshotDeserializer {

    private static final List<String> FACTORY_METHOD_NAMES = List.of("of", "from", "create", "valueOf");

    @SuppressWarnings("unchecked")
    public <A extends Aggregate> A deserialize(
            SnapshotEvent snapshot,
            A initialState,
            EventMetadata metadata
    ) {
        Class<A> aggregateClass = (Class<A>) initialState.getClass();
        Map<String, Object> state = snapshot.getState();

        try {
            A aggregate = createInstance(aggregateClass);
            Set<String> processedFields = new HashSet<>();

            for (Field field : getAllFields(aggregateClass)) {
                field.setAccessible(true);
                String fieldName = field.getName();
                processedFields.add(fieldName);

                if (field.isAnnotationPresent(SnapshotExclude.class)) {
                    if (isIdField(field)) {
                        field.set(aggregate, deserializeId(field.getType(), metadata.getAggregateId()));
                    } else if (isVersionField(field)) {
                        field.set(aggregate, metadata.getAggregateVersion());
                    }
                    continue;
                }

                if (state.containsKey(fieldName)) {
                    Object rawValue = state.get(fieldName);
                    Object value = deserializeValue(rawValue, field.getGenericType());
                    field.set(aggregate, value);
                } else {
                    log.debug(
                            "Field '{}' in aggregate {} not found in snapshot (may have been added after snapshot was created)",
                            fieldName,
                            aggregateClass.getSimpleName()
                    );
                }
            }

            Set<String> extraFields = new HashSet<>(state.keySet());
            extraFields.removeAll(processedFields);
            if (!extraFields.isEmpty()) {
                log.debug(
                        "Snapshot for {} contains fields not present in aggregate (may have been removed): {}",
                        aggregateClass.getSimpleName(),
                        extraFields
                );
            }

            return aggregate;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize snapshot for " + aggregateClass.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <A extends Aggregate> A createInstance(Class<A> clazz) throws Exception {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                constructor.setAccessible(true);
                return (A) constructor.newInstance();
            }
        }

        Constructor<?> constructor = constructors[0];
        constructor.setAccessible(true);

        Object[] args = new Object[constructor.getParameterCount()];
        Class<?>[] paramTypes = constructor.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = getDefaultValue(paramTypes[i]);
        }

        return (A) constructor.newInstance(args);
    }

    private @Nullable Object deserializeValue(Object value, Type targetType) {
        if (value == null) {
            return null;
        }

        Class<?> targetClass = getClassFromType(targetType);

        if (targetClass == String.class) {
            return value.toString();
        }

        if (targetClass == boolean.class || targetClass == Boolean.class) {
            return value instanceof Boolean ? value : Boolean.parseBoolean(value.toString());
        }

        if (targetClass == int.class || targetClass == Integer.class) {
            return value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString());
        }

        if (targetClass == long.class || targetClass == Long.class) {
            return value instanceof Number ? ((Number) value).longValue() : Long.parseLong(value.toString());
        }

        if (targetClass == double.class || targetClass == Double.class) {
            return value instanceof Number ? ((Number) value).doubleValue() : Double.parseDouble(value.toString());
        }

        if (targetClass == float.class || targetClass == Float.class) {
            return value instanceof Number ? ((Number) value).floatValue() : Float.parseFloat(value.toString());
        }

        if (targetClass == Instant.class) {
            return Instant.parse(value.toString());
        }

        if (targetClass == Duration.class) {
            return Duration.parse(value.toString());
        }

        if (targetClass == Version.class) {
            return Version.of(((Number) value).longValue());
        }

        if (targetClass.isEnum()) {
            return deserializeEnum(targetClass, value.toString());
        }

        if (Set.class.isAssignableFrom(targetClass)) {
            return deserializeCollection(value, targetType, true);
        }

        if (List.class.isAssignableFrom(targetClass) || Collection.class.isAssignableFrom(targetClass)) {
            return deserializeCollection(value, targetType, false);
        }

        if (Map.class.isAssignableFrom(targetClass)) {
            return deserializeMap(value, targetType);
        }

        return deserializeObject(value, targetClass);
    }

    private boolean isIdField(Field field) {
        return field.getName().equals("id") || field.getName().endsWith("Id");
    }

    private boolean isVersionField(Field field) {
        return field.getType() == Version.class && field.getName().equals("version");
    }

    private Object deserializeId(Class<?> idClass, AggregateId aggregateId) {
        if (idClass == String.class) {
            return aggregateId.getValue();
        }

        Object result = tryFactoryMethod(idClass, aggregateId.getValue());
        if (result != null) {
            return result;
        }

        result = tryConstructor(idClass, aggregateId.getValue());
        if (result != null) {
            return result;
        }

        throw new RuntimeException("Failed to deserialize ID of type " + idClass.getName() +
                ". No suitable factory method or constructor found.");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object deserializeEnum(Class<?> enumClass, String value) {
        return Enum.valueOf((Class<Enum>) enumClass, value);
    }

    private Object deserializeCollection(Object value, Type targetType, boolean asSet) {
        if (!(value instanceof Collection<?> collection)) {
            throw new RuntimeException("Expected collection but got: " + value.getClass());
        }

        Type elementType = Object.class;
        if (targetType instanceof ParameterizedType paramType) {
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length > 0) {
                elementType = typeArgs[0];
            }
        }

        Type finalElementType = elementType;
        List<Object> list = collection.stream()
                .map(item -> deserializeValue(item, finalElementType))
                .collect(Collectors.toList());

        return asSet ? new HashSet<>(list) : list;
    }

    private Object deserializeMap(Object value, Type targetType) {
        if (!(value instanceof Map<?, ?> map)) {
            throw new RuntimeException("Expected map but got: " + value.getClass());
        }

        Type keyType = String.class;
        Type valueType = Object.class;
        if (targetType instanceof ParameterizedType paramType) {
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length >= 2) {
                keyType = typeArgs[0];
                valueType = typeArgs[1];
            }
        }

        Map<Object, Object> result = new LinkedHashMap<>();
        Type finalKeyType = keyType;
        Type finalValueType = valueType;
        map.forEach((k, v) -> result.put(
                deserializeValue(k, finalKeyType),
                deserializeValue(v, finalValueType)
        ));
        return result;
    }

    private Object deserializeObject(Object value, Class<?> targetClass) {
        if (!(value instanceof Map)) {
            Object result = tryFactoryMethod(targetClass, value);
            if (result != null) {
                return result;
            }

            result = tryConstructor(targetClass, value);
            if (result != null) {
                return result;
            }
        }

        if (value instanceof Map<?, ?> map) {
            return deserializeFromMap(map, targetClass);
        }

        throw new RuntimeException("Cannot deserialize " + value.getClass() + " to " + targetClass.getName() +
                ". No suitable factory method, constructor, or map-based instantiation available.");
    }

    private @Nullable Object tryFactoryMethod(Class<?> clazz, Object value) {
        for (String methodName : FACTORY_METHOD_NAMES) {
            Method method = findStaticMethod(clazz, methodName, 1);
            if (method != null) {
                try {
                    method.setAccessible(true);
                    Object arg = deserializeValue(value, method.getParameterTypes()[0]);
                    return method.invoke(null, arg);
                } catch (Exception e) {
                    log.trace("Factory method {}() on {} failed: {}", methodName, clazz.getSimpleName(), e.getMessage());
                }
            }
        }
        return null;
    }

    private @Nullable Object tryConstructor(Class<?> clazz, Object value) {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == 1) {
                try {
                    constructor.setAccessible(true);
                    Object arg = deserializeValue(value, constructor.getParameterTypes()[0]);
                    return constructor.newInstance(arg);
                } catch (Exception e) {
                    log.trace("Constructor on {} failed: {}", clazz.getSimpleName(), e.getMessage());
                }
            }
        }
        return null;
    }

    private Object deserializeFromMap(Map<?, ?> map, Class<?> targetClass) {
        Object result = tryFactoryMethodForMap(targetClass, map);
        if (result != null) {
            return result;
        }

        result = tryConstructorForMap(targetClass, map);
        if (result != null) {
            return result;
        }

        result = tryFieldBasedInstantiation(targetClass, map);
        if (result != null) {
            return result;
        }

        throw new RuntimeException("Failed to create instance of " + targetClass.getName() +
                ". No suitable factory method, constructor, or field-based instantiation available.");
    }

    private @Nullable Object tryFactoryMethodForMap(Class<?> clazz, Map<?, ?> map) {
        for (String methodName : FACTORY_METHOD_NAMES) {
            for (Method method : clazz.getMethods()) {
                if (Modifier.isStatic(method.getModifiers())
                        && methodName.equals(method.getName())
                        && method.getParameterCount() > 0) {
                    try {
                        method.setAccessible(true);
                        Object[] args = buildArgsFromMap(method.getParameters(), map);
                        if (args != null) {
                            return method.invoke(null, args);
                        }
                    } catch (Exception e) {
                        log.trace("Factory method {}() on {} failed: {}", methodName, clazz.getSimpleName(), e.getMessage());
                    }
                }
            }
        }
        return null;
    }

    private @Nullable Object tryConstructorForMap(Class<?> clazz, Map<?, ?> map) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Arrays.sort(constructors, (c1, c2) -> Integer.compare(c2.getParameterCount(), c1.getParameterCount()));

        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                continue;
            }
            try {
                constructor.setAccessible(true);
                Object[] args = buildArgsFromMap(constructor.getParameters(), map);
                if (args != null) {
                    return constructor.newInstance(args);
                }
            } catch (Exception e) {
                log.trace("Constructor on {} failed: {}", clazz.getSimpleName(), e.getMessage());
            }
        }
        return null;
    }

    private @Nullable Object[] buildArgsFromMap(Parameter[] params, Map<?, ?> map) {
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            String paramName = params[i].getName();
            if (!map.containsKey(paramName)) {
                return null;
            }
            Object rawValue = map.get(paramName);
            args[i] = deserializeValue(rawValue, params[i].getParameterizedType());
        }
        return args;
    }

    private @Nullable Object tryFieldBasedInstantiation(Class<?> clazz, Map<?, ?> map) {
        try {
            Object instance = createObjectInstance(clazz);
            for (Field field : getAllFields(clazz)) {
                field.setAccessible(true);
                String fieldName = field.getName();
                if (map.containsKey(fieldName)) {
                    Object rawValue = map.get(fieldName);
                    Object deserializedValue = deserializeValue(rawValue, field.getGenericType());
                    field.set(instance, deserializedValue);
                }
            }
            return instance;
        } catch (Exception e) {
            log.trace("Field-based instantiation on {} failed: {}", clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }

    private Object createObjectInstance(Class<?> clazz) throws Exception {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            constructor.setAccessible(true);
            Object[] args = new Object[constructor.getParameterCount()];
            Class<?>[] paramTypes = constructor.getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++) {
                args[i] = getDefaultValue(paramTypes[i]);
            }
            return constructor.newInstance(args);
        }
        throw new RuntimeException("No constructor found for " + clazz.getName());
    }

    private @Nullable Method findStaticMethod(Class<?> clazz, String name, int paramCount) {
        for (Method method : clazz.getMethods()) {
            if (Modifier.isStatic(method.getModifiers())
                    && name.equals(method.getName())
                    && method.getParameterCount() == paramCount) {
                return method;
            }
        }
        return null;
    }

    private Class<?> getClassFromType(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        if (type instanceof ParameterizedType paramType) {
            return (Class<?>) paramType.getRawType();
        }
        return Object.class;
    }

    private @Nullable Object getDefaultValue(Class<?> clazz) {
        if (clazz == boolean.class) return false;
        if (clazz == int.class) return 0;
        if (clazz == long.class) return 0L;
        if (clazz == double.class) return 0.0;
        if (clazz == float.class) return 0.0f;
        if (clazz == char.class) return '\0';
        if (clazz == byte.class) return (byte) 0;
        if (clazz == short.class) return (short) 0;
        return null;
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
