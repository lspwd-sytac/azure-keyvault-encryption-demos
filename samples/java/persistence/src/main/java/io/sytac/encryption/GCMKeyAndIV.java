package io.sytac.encryption;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.crypto.spec.GCMParameterSpec;
import java.security.spec.AlgorithmParameterSpec;

@Data
@Builder @NoArgsConstructor @AllArgsConstructor
public class GCMKeyAndIV implements KeyAndIV {
    @Builder.Default
    private String cipherName = "AES/GCM/NoPadding";
    private InitializationVector IV;
    private EncryptionKey key;

    /**
     * Desired length of the authentication tag, in bits.
     */
    private int tagLength;

    @Override
    public AlgorithmParameterSpec createAlgorithmSpec() {
        return new GCMParameterSpec(tagLength, IV.getValue());
    }
}
