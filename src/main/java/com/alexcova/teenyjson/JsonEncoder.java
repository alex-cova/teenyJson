package com.alexcova.teenyjson;

import com.alexcova.teenyjson.api.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * A simple JSON encoder that can serialize objects to a JSON string.
 */
final class JsonEncoder {

    private final Map<Class<?>, Mapper> cache = new ConcurrentHashMap<>();
    private final Map<Class<?>, ValueSerializer<?>> serializers = new ConcurrentHashMap<>();
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    SimpleDateFormat getDateFormatter() {
        return dateFormatter;
    }

    /**
     * Registers a serializer for a specific class.
     *
     * @param clazz the class to register
     */
    public <T> void registerSerializer(Class<T> clazz, ValueSerializer<T> serializer) {
        serializers.put(clazz, serializer);
    }

    /**
     * Writes an object to a JSON string.
     *
     * @param value the object to be written
     * @return the JSON string
     */
    public @Nullable String writeValueAsString(Object value) {
        return switch (value) {
            case null -> null;
            case String s -> s;
            case Collection<?> collection -> "[" + collection.stream()
                    .map(this::serialize)
                    .collect(Collectors.joining(", ")) + "]";
            default -> serialize(value);
        };

    }


    /**
     * Serializes an object to a JSON string.
     *
     * @param object the object to be serialized
     * @return the JSON string
     */
    private String serialize(Object object) {
        if (object == null) return "null";

        Mapper cachedMapper = cache.get(object.getClass());

        if (cachedMapper != null) {
            return cachedMapper.serialize(object, this);
        }

        if (object instanceof Map<?, ?> map) {
            return "{" + map.entrySet()
                    .stream()
                    .map(entry -> "\"" + entry.getKey() + "\":" + serialize(entry.getValue()))
                    .collect(Collectors.joining(",")) + "}";
        }

        if (object instanceof Collection<?> list) {
            return "[" + list.stream()
                    .map(this::serialize)
                    .collect(Collectors.joining(",")) + "]";
        }

        if (object.getClass().isArray()) {
            return serializeArray(object);
        }

        final Class<?> clazz = object.getClass();

        if (clazz.getName().startsWith("java.lang")) {
            return writeValue(object);
        }

        Method[] methods = clazz.getDeclaredMethods();

        Mapper mapper = new Mapper(clazz);
        cache.put(clazz, mapper);

        for (Method method : methods) {
            try {
                if (method.getName().equals("hashCode")) continue;
                if (method.getName().equals("toString")) continue;

                mapper.put(method);
            } catch (Throwable e) {
                Logger.getLogger(JsonEncoder.class.getName())
                        .log(Level.SEVERE, "Error serializing object: " + object.getClass().getName(), e);
            }
        }

        return mapper.serialize(object, this);
    }

    /**
     * Serializes an array to a JSON string.
     *
     * <p>This method handles both arrays of objects and arrays of primitives.
     * For arrays of objects, it serializes each object in the array and joins them with a comma.
     * For arrays of primitives, it converts the array to a string representation.
     *
     * @param object the array to be serialized, which can be an array of objects or an array of primitives
     * @return the JSON string representation of the array
     * @throws IllegalArgumentException if the array is of a type that isn't handled
     */
    private @NotNull String serializeArray(@Nullable Object object) {
        if (object == null) return "null";

        return switch (object) {
            case Object[] array -> "[" + Arrays.stream(array)
                    .map(this::serialize)
                    .collect(Collectors.joining(", ")) + "]";
            case int[] array -> Arrays.toString(array).replace(" ", "");
            case double[] array -> Arrays.toString(array).replace(" ", "");
            case long[] array -> Arrays.toString(array).replace(" ", "");
            case char[] array -> Arrays.toString(array).replace(" ", "");
            case float[] array -> Arrays.toString(array).replace(" ", "");
            case boolean[] array -> Arrays.toString(array).replace(" ", "");
            case byte[] array -> Arrays.toString(array).replace(" ", "");
            case short[] array -> Arrays.toString(array).replace(" ", "");
            case null, default ->
                    throw new IllegalArgumentException("Unknown array type: " + object.getClass().getName());
        };
    }

    /**
     * Writes a field to a JSON string.
     *
     * @param name the name of the field
     * @param value the value of the field
     * @param includeNonNull whether to include null values or not
     * @return the JSON representation of the field
     */
    private String writeField(String name, Object value, boolean includeNonNull, AccessorInvoker invoker) {
        if (includeNonNull && value == null) return null;

        if (invoker.isAnnotationPresent(JsonRaw.class) && value instanceof String) {
            if (invoker.getAnnotation(JsonRaw.class).includeKey()) {
                return "\"" + name + "\":" + value;
            }
            return (String) value;
        }

        if (invoker.isAnnotationPresent(JsonIncludeNonNull.class) && value == null) return null;

        StringBuilder sb = new StringBuilder();

        try {
            sb.append("\"").append(name).append("\":");

            if (value instanceof Map) {
                sb.append("{");

                String map = ((Map<?, ?>) value)
                        .entrySet()
                        .stream()
                        .map(entry -> "\"" + entry.getKey() + "\":" + serialize(entry.getValue()))
                        .collect(Collectors.joining(", "));

                return sb.append(map)
                        .append("}")
                        .toString();
            }

            if (value instanceof Collection) {
                sb.append("[");

                String collection = ((Collection<?>) value)
                        .stream()
                        .map(this::serialize)
                        .collect(Collectors.joining(","));

                return sb.append(collection)
                        .append("]")
                        .toString();
            }

            sb.append(writeValue(value));
        } catch (Exception e) {
            Logger.getLogger(JsonEncoder.class.getName())
                    .log(Level.SEVERE, null, e);
        }

        return sb.toString();
    }

    /**
     * Gets the field name of a method.
     * If the method is annotated with {@link JsonAlias}, the value of the annotation is returned.
     *
     * @param method the method to get the field name from
     * @return the field name
     */
    private static @NotNull String getFieldName(@NotNull Method method) {
        if (method.isAnnotationPresent(JsonAlias.class)) {
            String alias = method.getAnnotation(JsonAlias.class).value();

            if (!alias.isEmpty()) {
                return alias;
            }
        }

        String name = method.getName();

        if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        }

        if (name.startsWith("is")) {
            name = name.substring(2);
        }

        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    /**
     * Writes the value as a valid JSON value.
     * @param value the value to be written
     * @return the JSON representation of the value
     */
    @Contract("null -> !null")
    @SuppressWarnings({"unchecked", "rawtypes"})
    private @Nullable String writeValue(Object value) {
        if (value == null) return "null";

        ValueSerializer serializer = serializers.get(value.getClass());

        if (serializer != null) {
            return serializer.serialize(value);
        }

        switch (value) {
            case String s -> {
                return "\"" + escapeJsonString(s) + "\"";
            }
            case Number number -> {
                return number.toString();
            }
            case Boolean b -> {
                return b.toString();
            }
            case Date date -> {
                return "\"" + dateFormatter.format(date) + "\"";
            }
            case Character c -> {
                return "\"" + c + "\"";
            }
            case LocalDate localDate -> {
                return "\"" + localDate + "\"";
            }
            case LocalDateTime localDateTime -> {
                return "\"" + localDateTime + "\"";
            }
            case LocalTime localTime -> {
                return "\"" + localTime + "\"";
            }
            default -> {
            }
        }

        if (value.getClass().isEnum()) {
            return "\"" + value + "\"";
        }

        if (!value.getClass().getName().startsWith("java.lang") && !value.getClass().getName().startsWith("java.time")) {
            return serialize(value);
        }

        System.err.println("Unknown type: " + value.getClass().getName());

        return null;
    }

    /**
     * Escapes a string to be a valid JSON string.
     *
     * @param s the string to be escaped
     * @return the escaped string
     */
    private String escapeJsonString(String s) {
        if (s == null) return null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '"':
                case '\\':
                    sb.append('\\');
                    sb.append(ch);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (ch <= 0x1F) {
                        String ss = Integer.toHexString(ch);
                        sb.append("\\u");
                        sb.append("0".repeat(4 - ss.length()));
                        sb.append(ss.toUpperCase());
                    } else {
                        sb.append(ch);
                    }
            }
        }
        return sb.toString();
    }

    /**
     * This class holds all lambdas to call the getter methods of a class.
     * It also holds the target class and a flag to include or not null values.
     * It is used to serialize an object to a JSON string.
     */
    private static class Mapper extends ConcurrentHashMap<String, AccessorInvoker> {
        private final Class<?> target;
        private final boolean includeNonNull;

        public Mapper(@NotNull Class<?> target) {
            this.target = target;
            includeNonNull = target.isAnnotationPresent(JsonIncludeNonNull.class);
        }

        private void put(@NotNull Method method) throws Throwable {
            if (method.isAnnotationPresent(JsonIgnore.class)) return;
            if (Modifier.isStatic(method.getModifiers())) return;
            if (method.getParameterCount() > 0) return;

            String fieldName = getFieldName(method);
            put(fieldName, createFunction(target, method));
        }

        /**
         * Serializes an object to a JSON string.
         * @param object the object to be serialized
         * @param encoder the encoder to be used
         * @return the JSON string
         */
        private @NotNull String serialize(Object object, JsonEncoder encoder) {
            List<String> properties = new LinkedList<>();

            for (Entry<String, AccessorInvoker> entry : entrySet()) {
                Object value = entry.getValue().invoke(object);
                if (value == null && !includeNonNull) continue;
                String field = encoder.writeField(entry.getKey(), value, includeNonNull, entry.getValue());
                if (field == null) continue;

                properties.add(field);
            }

            return "{" + String.join(",", properties) + "}";
        }
    }

    /**
     * Creates a function to call the getter method of a class.
     *
     * @param targetClass the class to create the function for
     * @param method the method to call
     * @return the function to call the method
     * @throws Throwable if an error occurs while creating the function
     */
    @Contract("_, _ -> new")
    private static @NotNull AccessorInvoker createFunction(Class<?> targetClass, @NotNull Method method) throws Throwable {
        try {
            MethodHandles.Lookup lookup = getLookup(targetClass);
            MethodHandle virtualMethodHandle = lookup.findVirtual(targetClass, method.getName(), MethodType.methodType(method.getReturnType()));
            CallSite site = LambdaMetafactory.metafactory(lookup,
                    "apply",
                    MethodType.methodType(Function.class),
                    MethodType.methodType(Object.class, Object.class),
                    virtualMethodHandle,
                    MethodType.methodType(method.getReturnType(), targetClass));
            @SuppressWarnings("unchecked")
            Function<Object, Object> getterFunction = (Function<Object, Object>) site.getTarget().invokeExact();
            return new AccessorInvoker(method, getterFunction);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to create function for " + targetClass + " at " + method.getName(), e);
        }
    }

    /**
     * Gets the lookup object for a class.
     * If the class is not accessible, a private lookup is created.
     *
     * @param targetClass the class to get the lookup for
     * @return the lookup object
     */
    private static MethodHandles.Lookup getLookup(Class<?> targetClass) {
        MethodHandles.Lookup lookupMe = MethodHandles.lookup();

        try {
            return MethodHandles.privateLookupIn(targetClass, lookupMe);
        } catch (IllegalAccessException e) {
            return lookupMe;
        }
    }
}
