package io.sytac.azure.demos.persistence.encryption;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class InitializationVector {
    private byte[] value;
}
