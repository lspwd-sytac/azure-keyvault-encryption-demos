package io.sytac.azure.demos.persistence.encryption;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class EncryptionKey {
    private byte[] value;
}
