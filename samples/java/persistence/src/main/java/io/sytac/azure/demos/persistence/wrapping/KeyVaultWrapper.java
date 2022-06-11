package io.sytac.azure.demos.persistence.wrapping;

import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sytac.azure.demos.persistence.encryption.AES;
import io.sytac.azure.demos.persistence.encryption.KeyAndIV;
import io.sytac.azure.demos.persistence.encryption.KeyAndIVFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@RequiredArgsConstructor
public class KeyVaultWrapper {

    private static final Base64.Encoder b64Enc = Base64.getEncoder();
    private static final Base64.Decoder b64Dec = Base64.getDecoder();

    @NonNull
    private CryptographyClient kvCryptoClient;

    @NonNull
    private ObjectMapper objectMapper;

    private final KeyWrapAlgorithm wrapAlgorithm = KeyWrapAlgorithm.RSA_OAEP_256;

    public <T> EncryptedObjectFragment encrypt(T other) throws IOException {
        return encrypt(other, KeyAndIVFactory.defaultRandom());
    }

    public <T> EncryptedObjectFragment encrypt(T other, KeyAndIV kiv) throws IOException {
        try {
            var wrappedKey = kvCryptoClient.wrapKey(wrapAlgorithm, objectMapper.writeValueAsBytes(kiv)).getEncryptedKey();
            var encObject = AES.encrypt(kiv, objectMapper.writeValueAsBytes(other));

            return EncryptedObjectFragment.builder()
                    .wrappedKey(b64Enc.encodeToString(wrappedKey))
                    .marshalledValue(b64Enc.encodeToString(encObject.getCiphertext()))
                    .tag(b64Enc.encodeToString(encObject.getTag()))
                    .kek(KeyReference.builder()
                            .name(kvCryptoClient.getKey().getName())
                            .version(kvCryptoClient.getKey().getId())
                            .build())
                    .build();
        } catch (InvalidAlgorithmParameterException
                 | NoSuchPaddingException
                 | IllegalBlockSizeException
                 | NoSuchAlgorithmException
                 | BadPaddingException
                 | InvalidKeyException ex) {
            throw new IOException(ex);
        }
    }

    public <T> T decrypt(EncryptedObjectFragment eof, Class<T> clazz) throws IOException {
        try {
            var kivBytes = kvCryptoClient.unwrapKey(wrapAlgorithm, b64Dec.decode(eof.getWrappedKey())).getKey();
            KeyAndIV kiv = objectMapper.readValue(kivBytes, KeyAndIV.class);

            var ciphertext = b64Dec.decode(eof.getMarshalledValue());
            var tag = b64Dec.decode(eof.getTag());
            var plainPayload = AES.decrypt(kiv, ciphertext, tag);
            return objectMapper.readValue(plainPayload, clazz);
        } catch (InvalidAlgorithmParameterException
                 | NoSuchPaddingException
                 | IllegalBlockSizeException
                 | NoSuchAlgorithmException
                 | BadPaddingException
                 | InvalidKeyException ex) {
            throw new IOException(ex);
        }
    }
}
