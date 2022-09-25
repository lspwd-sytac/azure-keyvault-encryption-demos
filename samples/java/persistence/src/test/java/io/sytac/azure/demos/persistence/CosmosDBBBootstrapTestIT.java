package io.sytac.azure.demos.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return new CosmosDBPersistence<>(coll, mapper, SampleObject.class);
    }
}
