package io.sytac.encryption;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class InitializationVector {
    private byte[] value;
}
