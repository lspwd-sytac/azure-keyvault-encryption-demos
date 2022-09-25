package io.sytac.azure.demos.persistence;

import lombok.NonNull;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.Map;

public interface ApplicationPersistence<TVal> {
    String store(@NonNull TVal val, @NonNull ObjectIdSupplier<TVal> idSupplier) throws IOException;

    ObjectId idOf(Document key) throws IOException;

    DBIdentifiedObject<TVal> get(@NonNull Map<String, Object> reqKey) throws IOException;
}
