package io.sytac.azure.demos.persistence.wrapping;

import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sytac.azure.demos.persistence.encryption.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.crypto.BadPaddingException;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;

@RequiredArgsConstructor
public class KeyVaultWrapper {

    static final Base64.Encoder b64Enc = Base64.getEncoder();
    static final Base64.Decoder b64Dec = Base64.getDecoder();

    @NonNull
    private CryptographyClient kvCryptoClient;

    @NonNull
    private ObjectMapper objectMapper;

    private final KeyWrapAlgorithm wrapAlgorithm = KeyWrapAlgorithm.RSA_OAEP_256;

    public <ILogical, TStorage extends StoredEncryptedObject, TProtected> TStorage wrap(ILogical inst, StorageMapper<ILogical, TStorage, TProtected> mapper) throws IOException {
        var storeObj = mapper.mapStorageStruct(inst);
        var protectedPart = mapper.mapProtectedStruct(inst);
        var enc = encrypt(protectedPart, storeObj.getAdditionalAuthenticationData());

        storeObj.setEncryptedFragment(enc);
        return storeObj;
    }

    public <ILogical, TStorage extends StoredEncryptedObject, TProtected> ILogical unwrap(TStorage storage, StorageMapper<ILogical, TStorage, TProtected> mapper) throws IOException, BadPaddingException {
        var decryptedProtectedPart = decrypt(storage.getEncryptedFragment(), storage.getAdditionalAuthenticationData(), mapper.getProtectedStructClass());
        return mapper.restore(storage, decryptedProtectedPart);
    }

    public <T> EncryptedObjectFragment encrypt(T other) throws IOException {
        return encrypt(other, null);
    }

    public <T> EncryptedObjectFragment encrypt(T other, AdditionalAuthenticationData aad) throws IOException {
        return encrypt(other, KeyAndIVFactory.defaultRandom(), aad);
    }

    public <T> EncryptedObjectFragment encrypt(T other, KeyAndIV kiv, AdditionalAuthenticationData aad) throws IOException {
        var encObject = AES.encrypt(kiv, aad, new BinaryPlaintext(objectMapper.writeValueAsBytes(other)));
        var wrappedKey = kvCryptoClient.wrapKey(wrapAlgorithm, objectMapper.writeValueAsBytes(kiv)).getEncryptedKey();

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
        var kivBytes = kvCryptoClient.unwrapKey(wrapAlgorithm, b64Dec.decode(eof.getWrappedKey())).getKey();
        KeyAndIV kiv = objectMapper.readValue(kivBytes, KeyAndIV.class);

        var plainPayload = AES.decrypt(kiv, aad, eof.getAuthenticatedCipherText());
        return objectMapper.readValue(plainPayload.getValue(), clazz);
    }
}
