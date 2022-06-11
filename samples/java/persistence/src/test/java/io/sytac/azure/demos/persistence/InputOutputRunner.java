package io.sytac.azure.demos.persistence;

import com.mongodb.Function;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InputOutputRunner {

    static void runCycle(String collectionName, Function<MongoCollection<Document>, ApplicationPersistence<SampleObject>> supplier) throws IOException {
        String connectionString = System.getenv().get("MONGO_URL");
        assertNotNull(connectionString);

        try (var cl = MongoClients.create(connectionString)) {
            var coll = cl.getDatabase("demo").getCollection(collectionName);
            // Clear out the collection
            coll.deleteMany(new Document());

            var persister = supplier.apply(coll);

            var exampleWrite = SampleObject.builder()
                    .guid("a-b-c")
                    .value("12345")
                    .secretValue("This is something you should only see on the console")
                    .build();
            persister.store(exampleWrite, SampleObject::identify);

            var exampleRead = persister.get(Collections.singletonMap("guid", "a-b-c"));
            assertEquals("12345", exampleRead.getValue());
            System.out.println(exampleRead.getSecretValue());
        }
    }
}
