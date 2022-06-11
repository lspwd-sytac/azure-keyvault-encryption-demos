package io.sytac.azure.demos.persistence;

import org.bson.conversions.Bson;

import java.io.Serializable;
import java.util.Map;

/**
 * @param <TVal> generified object value type
 */
@FunctionalInterface
public interface ObjectIdSupplier<TVal> {

    /**
     * Supplies the identifier of the object
     *
     * @param val value of the object
     * @return value of the id
     */
    Map<String, Object> supplyIdOf(TVal val);
}
