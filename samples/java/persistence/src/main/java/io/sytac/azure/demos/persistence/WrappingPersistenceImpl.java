package io.sytac.azure.demos.persistence;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bson.Document;

import javax.crypto.BadPaddingException;
import java.io.IOException;
import java.util.Map;

/**
 * Application persistence that will encrypt data for the storage in the database
 */
@AllArgsConstructor
public class WrappingPersistenceImpl<TApp, TSchema> implements ApplicationPersistence<TApp>{

    @NonNull
    private ProtectionDecorator<TApp, TSchema> protectionDecorator;
    @NonNull CosmosDBPersistenceImpl<TSchema> storage;

    @Override
    public void store(@NonNull TApp tApp, @NonNull ObjectIdSupplier<TApp> idSupplier) throws IOException {
        storage.store(protectionDecorator.apply(tApp), new Document(idSupplier.supplyIdOf(tApp)));
    }

    @Override
    public TApp get(@NonNull Map<String, Object> reqKey) throws IOException {
        var schemaObject = storage.get(reqKey);
        if (schemaObject != null) {
            return protectionDecorator.restore(schemaObject);
        } else {
            return null;
        }
    }
}
