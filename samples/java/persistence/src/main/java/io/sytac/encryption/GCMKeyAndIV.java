package io.sytac.encryption;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.crypto.spec.GCMParameterSpec;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

@Data
@Builder @NoArgsConstructor @AllArgsConstructor
public class GCMKeyAndIV implements KeyAndIV {

    static final Base64.Encoder b64Enc = Base64.getEncoder();
    static final Base64.Decoder b64Dec = Base64.getDecoder();

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

    @Override
    public SerializedKeyAndIV serialize() {
        return SerializedKeyAndIV.builder()
                .cipherName(getCipherName())
                .key(b64Enc.encodeToString(this.key.getValue()))
                .iv(b64Enc.encodeToString(this.IV.getValue()))
                .tagLength(getTagLength())
                .build();
    }

    public static GCMKeyAndIV deserialize(SerializedKeyAndIV skiv) {
       return GCMKeyAndIV.builder()
               .cipherName(skiv.getCipherName())
               .key(new EncryptionKey(b64Dec.decode(skiv.getKey())))
               .IV(new InitializationVector(b64Dec.decode(skiv.getIv())))
               .tagLength(skiv.getTagLength())
               .build();
    }
}
