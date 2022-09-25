package io.sytac.azure.demos.persistence;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.Map;

/**
 * Application persistence that will encrypt data for the storage in the database
 */
@AllArgsConstructor
public class ProtectionDecoratingPersistence<TApp, TSchema> implements ApplicationPersistence<TApp>{

    @NonNull
    private ProtectionDecorator<TApp, TSchema> protectionDecorator;
    @NonNull CosmosDBPersistence<TSchema> storage;

    @Override
    public String store(@NonNull TApp tApp, @NonNull ObjectIdSupplier<TApp> idSupplier) throws IOException {
        Document ident = new Document(idSupplier.supplyIdOf(tApp));
        String aad;

        var objID = storage.idOf(ident);
        if (objID == null) {
            aad = storage.store(ident, ident);
        } else {
            aad = objID.toString();
        }

        return storage.store(protectionDecorator.apply(tApp, aad), ident);
    }

    @Override
    public DBIdentifiedObject<TApp> get(@NonNull Map<String, Object> reqKey) throws IOException {
        var schemaObject = storage.get(reqKey);
        if (schemaObject != null) {
            return schemaObject.mapTo(protectionDecorator.restore(schemaObject.getValue(), schemaObject.getDatabaseId()));
        } else {
            return null;
        }
    }

    @Override
    public ObjectId idOf(Document key) throws IOException {
        return storage.idOf(key);
    }
}
