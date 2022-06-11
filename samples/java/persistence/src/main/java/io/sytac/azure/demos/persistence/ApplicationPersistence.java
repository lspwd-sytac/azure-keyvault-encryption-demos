package io.sytac.azure.demos.persistence;

import lombok.NonNull;

import java.io.IOException;
import java.util.Map;

public interface ApplicationPersistence<TVal> {
    void store(@NonNull TVal val, @NonNull ObjectIdSupplier<TVal> idSupplier) throws IOException;

    TVal get(@NonNull Map<String, Object> reqKey) throws IOException;
}
