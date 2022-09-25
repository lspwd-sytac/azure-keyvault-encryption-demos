package io.sytac.azure.demos.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Basic CosmosDB read and write without any additional protection.
 *
 * @param <TSchema> schema of the stored object
 */
@RequiredArgsConstructor
public class CosmosDBPersistence<TSchema> implements ApplicationPersistence<TSchema> {

    private static final UpdateOptions upsertOptions = new UpdateOptions().upsert(true);

    @NonNull
    private MongoCollection<Document> collection;
    @NonNull
    private ObjectMapper mapper;

    @NonNull
    private Class<TSchema> schemaClass;

    @Override
    public String store(@NonNull TSchema val, @NonNull ObjectIdSupplier<TSchema> idSupplier) {
        return store(val, new Document(idSupplier.supplyIdOf(val)));
    }

    @SuppressWarnings("unchecked")
    public String store(@NotNull TSchema val, Document key) {
        return store(key, mapper.convertValue(val, Map.class));
    }

    public String store(Document key, Map<String, Object> rawJSON) {
        var updates = rawJSON.entrySet()
                .stream()
                .map(e -> Updates.set(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        var ur = collection.updateOne(key, Updates.combine(updates), upsertOptions);
        return ur.getUpsertedId() != null ? ((BsonObjectId)ur.getUpsertedId()).getValue().toString() : null;
    }

    @Override
    public ObjectId idOf(Document key) throws IOException {
        var findResults = collection.find(key);

        try (var i = findResults.iterator()) {
            return ofSingleMatch(i, (doc) -> doc.getObjectId("_id"));
        }
    }

    @Override
    public DBIdentifiedObject<TSchema> get(@NonNull Map<String, Object> reqKey) throws IOException {
        var key = new Document(reqKey);
        var findResults = collection.find(key);
        try (var i = findResults.iterator()) {
            return ofSingleMatch(i, (doc) -> new DBIdentifiedObject<>(doc.getObjectId("_id").toString(), mapper.convertValue(doc, schemaClass)));
        }
    }

    private <T> T ofSingleMatch(MongoCursor<Document> cursor, Function<Document, T> converter) throws IOException {
        if (cursor.hasNext()) {
            Document foundMatch = cursor.next();
            if (cursor.hasNext()) {
                throw new IOException("Multiple results for the same identifier");
            }
            return converter.apply(foundMatch);
        } else {
            return null;
        }
    }
}
