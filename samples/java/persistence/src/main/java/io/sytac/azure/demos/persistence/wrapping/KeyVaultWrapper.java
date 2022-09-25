package io.sytac.azure.demos.persistence.wrapping;

import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sytac.encryption.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.crypto.BadPaddingException;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.function.Function;

@RequiredArgsConstructor
public class KeyVaultWrapper {

    static final Base64.Encoder b64Enc = Base64.getEncoder();
    static final Base64.Decoder b64Dec = Base64.getDecoder();

    @NonNull
    private CryptographyClient kvCryptoClient;

    @NonNull
    private ObjectMapper objectMapper;

    private final KeyWrapAlgorithm wrapAlgorithm = KeyWrapAlgorithm.RSA_OAEP_256;

    public <T> EncryptedObjectFragment encrypt(T other) throws IOException {
        return encrypt(other, null);
    }

    public <T> EncryptedObjectFragment encrypt(T other, AdditionalAuthenticationData aad) throws IOException {
        return encrypt(other, GCMKeyAndIVFactory.defaultRandom(), aad);
    }

    public <T> EncryptedObjectFragment encrypt(T other, KeyAndIV kiv, AdditionalAuthenticationData aad) throws IOException {
        var encObject = AES.encrypt(kiv, aad, new BinaryPlaintext(objectMapper.writeValueAsBytes(other)));
        var wrappedKey = kvCryptoClient.wrapKey(wrapAlgorithm, objectMapper.writeValueAsBytes(kiv.serialize())).getEncryptedKey();

        return EncryptedObjectFragment.builder()
                .encryptionDate(Instant.now())
                .wrappedKey(b64Enc.encodeToString(wrappedKey))
                .kek(KeyReference.builder()
                        .name(kvCryptoClient.getKey().getName())
                        .version(kvCryptoClient.getKey().getId())
                        .build())
                .serializedValue(b64Enc.encodeToString(encObject.getCiphertext().getValue()))
                .serializedAuthenticationTag(b64Enc.encodeToString(encObject.getTag().getValue()))
                .build();
    }

    public <T> T decrypt(EncryptedObjectFragment eof, Class<T> clazz) throws IOException, BadPaddingException {
        return decrypt(eof, null, clazz);
    }

    public <T> T decrypt(EncryptedObjectFragment eof, AdditionalAuthenticationData aad, Class<T> clazz) throws IOException, BadPaddingException {
        return decrypt(eof, aad, clazz, GCMKeyAndIV::deserialize);
    }
    public <T> T decrypt(EncryptedObjectFragment eof, AdditionalAuthenticationData aad, Class<T> clazz, Function<SerializedKeyAndIV, KeyAndIV> deserializer) throws IOException, BadPaddingException {
        var kivBytes = kvCryptoClient.unwrapKey(wrapAlgorithm, b64Dec.decode(eof.getWrappedKey())).getKey();
        SerializedKeyAndIV skiv = objectMapper.readValue(kivBytes, SerializedKeyAndIV.class);
        KeyAndIV kiv = deserializer.apply(skiv);

        var plainPayload = AES.decrypt(kiv, aad, eof.getAuthenticatedCipherText());
        return objectMapper.readValue(plainPayload.getValue(), clazz);
    }
}
