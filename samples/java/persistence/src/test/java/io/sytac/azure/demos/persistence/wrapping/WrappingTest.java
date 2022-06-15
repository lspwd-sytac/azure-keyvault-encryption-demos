package io.sytac.azure.demos.persistence.wrapping;

import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.sytac.azure.demos.persistence.ProtectedSampleObject;
import io.sytac.azure.demos.persistence.SampleObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WrappingTest {

    private static KeyVaultWrapper wrapper;

    @BeforeAll
    static void setup() {
        ObjectMapper mapper = JsonMapper.builder()
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

        CryptographyClient cryptoClient = new CryptographyClientBuilder()
                .keyIdentifier(keyId)
                .credential(clientCertificateCredential)
                .buildClient();

        wrapper = new KeyVaultWrapper(cryptoClient, mapper);
    }

    @Test
    void testWillWrapAndUnwrapSuccessfully() throws IOException {
        var eof = wrapper.encrypt(SampleObject.builder().secretValue("AAAA").build());
        var decr = wrapper.decrypt(eof, SampleObject.class);

        assertEquals("AAAA", decr.getSecretValue());
    }

}
