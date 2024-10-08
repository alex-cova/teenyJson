package com.alexcova.teenyjson.api;

/**
 * A simple interface for parsing a value into T, where the value could be a Map, List, Null or String.
 */
public interface ValueParser<T> {
    T parse(Object value);

}
