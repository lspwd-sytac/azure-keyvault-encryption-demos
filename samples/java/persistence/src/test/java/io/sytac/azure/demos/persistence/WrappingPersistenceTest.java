package io.sytac.azure.demos.persistence;

import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.mongodb.client.MongoCollection;
import io.sytac.azure.demos.persistence.wrapping.EncryptedObjectFragment;
import io.sytac.azure.demos.persistence.wrapping.KeyVaultWrapper;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WrappingPersistenceTest {


    private static CryptographyClient cryptoClient;
    private static ObjectMapper mapper;

    private static KeyVaultWrapper wrapper;


    @BeforeAll
    static void setup() {
        mapper = JsonMapper.builder()
                .findAndAddModules()
                .build();

        var clientId = System.getenv().get("AZ_CLIENT_ID");
        var tenantId = System.getenv().get("AZ_TENANT_ID");
        var pemPath = System.getenv().get("AZ_PEM_PATH");

        assertNotNull(clientId);
        assertNotNull(tenantId);
        assertNotNull(pemPath);

        ClientCertificateCredential clientCertificateCredential = new ClientCertificateCredentialBuilder()
                .clientId(clientId)
                .tenantId(tenantId)
                .pemCertificate(pemPath)
                .build();

        var keyId = System.getenv().get("AZ_KEY_ID");
        assertNotNull(keyId);

        cryptoClient = new CryptographyClientBuilder()
                .keyIdentifier(keyId)
                .credential(clientCertificateCredential)
                .buildClient();

        wrapper = new KeyVaultWrapper(cryptoClient, mapper);
    }

    @Test
    public void testWillSerializeInstants() throws JsonProcessingException {
        ProtectedSampleObject pso = ProtectedSampleObject.builder()
                .eof(EncryptedObjectFragment.builder().build())
                .build();
        System.out.println(mapper.writeValueAsString(pso));
    }

    @Test
    public void testWillProtectDocumentInDatabase() throws IOException {
        InputOutputRunner.runCycle("secure", this::createPersistenceUnderTest);
    }

    @NotNull ApplicationPersistence<SampleObject> createPersistenceUnderTest(MongoCollection<Document> coll) {
        return new WrappingPersistenceImpl<>(
                new SampleObjectProtectionDecorator(),
                new CosmosDBPersistenceImpl<>(coll, mapper, ProtectedSampleObject.class)
        );
    }

    static class SampleObjectProtectionDecorator implements ProtectionDecorator<SampleObject, ProtectedSampleObject> {
        @Override
        public ProtectedSampleObject apply(SampleObject upon) throws IOException {
            return ProtectedSampleObject.builder()
                    .guid(upon.getGuid())
                    .value(upon.getValue())
                    .eof(wrapper.encrypt(protectedPartOf(upon)))
                    .build();
        }

        @Override
        public SampleObject restore(ProtectedSampleObject fromProtected) throws IOException {
            SampleObjectProtectedPart sopp = wrapper.decrypt(fromProtected.getEof(), SampleObjectProtectedPart.class);

            return SampleObject.builder()
                    .guid(fromProtected.getGuid())
                    .value(fromProtected.getValue())
                    .secretValue(sopp.getSecretValue())
                    .anotherSecretValue(sopp.getAnotherSecretValue())
                    .build();
        }

        private SampleObjectProtectedPart protectedPartOf(SampleObject so) {
            return SampleObjectProtectedPart.builder()
                    .secretValue(so.getSecretValue())
                    .anotherSecretValue(so.getAnotherSecretValue())
                    .build();
        }
    }

}
