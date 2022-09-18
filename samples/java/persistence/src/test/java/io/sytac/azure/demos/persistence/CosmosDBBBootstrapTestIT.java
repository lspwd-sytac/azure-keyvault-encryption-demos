package io.sytac.azure.demos.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class CosmosDBBBootstrapTestIT {

    @Test
    void testBasicReadWrite() throws IOException {
        InputOutputRunner.runCycle("insecure", this::createPersistenceUnderTest);
    }

    @NotNull
    protected ApplicationPersistence<SampleObject> createPersistenceUnderTest(MongoCollection<Document> coll) {
        return new CosmosDBPersistenceImpl<>(coll, new ObjectMapper(), SampleObject.class);
    }
}
