package io.sytac.encryption;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class EncryptionKey {
    private byte[] value;
}
