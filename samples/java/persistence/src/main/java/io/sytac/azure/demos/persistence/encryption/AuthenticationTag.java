package io.sytac.azure.demos.persistence.encryption;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data @Builder
@AllArgsConstructor
public class AuthenticationTag {
    private byte[] value;

    public AuthenticationTag(byte[] source, int from, int length) {
        this.value = new byte[length];
        System.arraycopy(source, from, this.value, 0, length);
    }
}
