package io.sytac.azure.demos.persistence.wrapping;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Encrypted object fragment
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EncryptedObjectFragment {
    @JsonProperty("v")
    private String marshalledValue;

    @JsonProperty("k")
    private String wrappedKey;

    @JsonProperty("t")
    private String tag;

    @JsonProperty("d") @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant encryptionDate = Instant.now();

    @JsonProperty("kek")
    private KeyReference kek;
}
