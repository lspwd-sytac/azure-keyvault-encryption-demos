package io.sytac.azure.demos.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Basic CosmosDB read and write without any additional protection.
 *
 * @param <TSchema> schema of the stored object
 */
@RequiredArgsConstructor
public class CosmosDBPersistenceImpl<TSchema> implements ApplicationPersistence<TSchema> {

    private static final UpdateOptions upsertOptions = new UpdateOptions().upsert(true);

    @NonNull
    private MongoCollection<Document> collection;
    @NonNull
    private ObjectMapper mapper;

    @NonNull
    private Class<TSchema> schemaClass;

    @Override
    public void store(@NonNull TSchema val, @NonNull ObjectIdSupplier<TSchema> idSupplier) throws IOException {
        store(val, new Document(idSupplier.supplyIdOf(val)));
    }

    @SuppressWarnings("unchecked")
    public void store(@NotNull TSchema val, Document key) {
        Map<String, Object> rawJSON = mapper.convertValue(val, Map.class);
        var updates = rawJSON.entrySet()
                .stream()
                .map(e -> Updates.set(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        collection.updateMany(key, Updates.combine(updates), upsertOptions);
    }

    @Override
    public TSchema get(@NonNull Map<String, Object> reqKey) throws IOException {
        var key = new Document(reqKey);
        var findResults = collection.find(key);
        try (var i = findResults.iterator()) {
            if (i.hasNext()) {
                return mapper.convertValue(i.next(), schemaClass);
            } else {
                return null;
            }
        }
    }
}
