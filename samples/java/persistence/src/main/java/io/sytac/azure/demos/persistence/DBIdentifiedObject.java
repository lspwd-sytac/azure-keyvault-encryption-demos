package io.sytac.azure.demos.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Container for the application object and its identification
 * @param <T>
 */
@AllArgsConstructor
@Getter
public class DBIdentifiedObject<T> {
    String databaseId;
    T value;

    public <V> DBIdentifiedObject<V> mapTo(V mappedValue) {
        return new DBIdentifiedObject<>(this.databaseId, mappedValue);
    }
}


