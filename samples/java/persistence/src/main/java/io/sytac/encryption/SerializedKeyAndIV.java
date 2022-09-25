package io.sytac.encryption;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
@Builder
public class SerializedKeyAndIV {
    private String cipherName;
    private String key;
    private String iv;
    private int tagLength;
}
