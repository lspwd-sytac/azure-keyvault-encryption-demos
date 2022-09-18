package io.sytac.encryption;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Ciphertext {
    private byte[] value;
}
