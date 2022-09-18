package io.sytac.azure.demos.persistence.wrapping;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.sytac.encryption.AuthenticatedCiphertext;
import io.sytac.encryption.AuthenticationTag;
import io.sytac.encryption.Ciphertext;
import io.sytac.encryption.GCMKeyAndIV;
import lombok.*;

import java.time.Instant;

/**
 * Encrypted object fragment is the data structure that stores subset of the object's fields that are encrypted with
 * a (one-time) encryption key which, in turn, is wrapped using a key encryption key {@link #getKek()}.
 * <p/>
 * An application needing access to this key should perform the following steps:
 * <ol>
 *     <li>Obtain access to the key encryption key (KEK) referenced by {@link #getKek()}</li>
 *     <li>Using the KEK, unwrap the value of {@link #getWrappedKey()}</li>
 *     <li>Restore the {@link GCMKeyAndIV}</li>
 *     <li>Restore the data by decrypting the {@link #getSerializedValue()}</li>
 * </ol>
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EncryptedObjectFragment {
    @JsonProperty("v")
    private String serializedValue;

    @JsonProperty("t")
    private String serializedAuthenticationTag;

    @JsonProperty("k")
    private String wrappedKey;


    @JsonProperty("d") @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant encryptionDate = Instant.now();

    @JsonProperty("kek")
    private KeyReference kek;

    @JsonIgnore
    public AuthenticatedCiphertext getAuthenticatedCipherText() {
        return new AuthenticatedCiphertext(new Ciphertext(KeyVaultWrapper.b64Dec.decode(serializedValue)),
                new AuthenticationTag(KeyVaultWrapper.b64Dec.decode(serializedAuthenticationTag)));
    }
}
